/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.owl.consumer

import dev.usbharu.owl.common.property.PropertySerializeUtils
import dev.usbharu.owl.common.property.PropertySerializerFactory
import dev.usbharu.owl.generated.*
import dev.usbharu.owl.generated.Uuid.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.math.max

/**
 * Consumer
 *
 * @property subscribeTaskStub
 * @property assignmentTaskStub
 * @property taskResultStub
 * @property runnerMap
 * @property propertySerializerFactory
 * @constructor
 * TODO
 *
 * @param consumerConfig
 */
class Consumer(
    private val subscribeTaskStub: SubscribeTaskServiceGrpcKt.SubscribeTaskServiceCoroutineStub,
    private val assignmentTaskStub: AssignmentTaskServiceGrpcKt.AssignmentTaskServiceCoroutineStub,
    private val taskResultStub: TaskResultServiceGrpcKt.TaskResultServiceCoroutineStub,
    taskRunnerLoader: TaskRunnerLoader,
    private val propertySerializerFactory: PropertySerializerFactory,
    consumerConfig: ConsumerConfig,
) {

    private lateinit var consumerId: UUID

    private lateinit var coroutineScope: CoroutineScope

    private val concurrent = MutableStateFlow(consumerConfig.concurrent)
    private val processing = MutableStateFlow(0)

    private val runnerMap = taskRunnerLoader.load()

    /**
     * Consumerを初期化します
     *
     * @param name Consumer名
     * @param hostname Consumerのホスト名
     */
    suspend fun init(name: String, hostname: String) {
        logger.info("Initialize Consumer name: {} hostname: {}", name, hostname)
        logger.debug("Registered Tasks: {}", runnerMap.keys)
        consumerId = subscribeTaskStub.subscribeTask(
            subscribeTaskRequest {
                this.name = name
                this.hostname = hostname
                this.tasks.addAll(runnerMap.keys)
            }
        ).id
        logger.info("Success initialize consumer. ConsumerID: {}", consumerId)
    }

    /**
     * タスクの受付を開始します
     *
     */
    suspend fun start() {
        coroutineScope = CoroutineScope(Dispatchers.Default)
        coroutineScope {
            while (isActive) {
                try {
                    taskResultStub
                        .tasKResult(
                            flow {
                                assignmentTaskStub
                                    .ready(
                                        flow {
                                            requestTask()
                                        }
                                    ).onEach {
                                        logger.info("Start Task name: {} id: {}", it.name, it.id)
                                        processing.update { it + 1 }

                                        try {
                                            val taskResult = runnerMap.getValue(it.name).run(
                                                TaskRequest(
                                                    it.name,
                                                    java.util.UUID(
                                                        it.id.mostSignificantUuidBits,
                                                        it.id.leastSignificantUuidBits
                                                    ),
                                                    it.attempt,
                                                    Instant.ofEpochSecond(
                                                        it.queuedAt.seconds,
                                                        it.queuedAt.nanos.toLong()
                                                    ),
                                                    PropertySerializeUtils.deserialize(
                                                        propertySerializerFactory,
                                                        it.propertiesMap
                                                    )
                                                )
                                            )

                                            emit(
                                                taskResult {
                                                    this.success = taskResult.success
                                                    this.attempt = it.attempt
                                                    this.id = it.id
                                                    this.result.putAll(
                                                        PropertySerializeUtils.serialize(
                                                            propertySerializerFactory,
                                                            taskResult.result
                                                        )
                                                    )
                                                    this.message = taskResult.message
                                                }
                                            )
                                            logger.info(
                                                "Success execute task. name: {} success: {}",
                                                it.name,
                                                taskResult.success
                                            )
                                            logger.debug("TRACE RESULT {}", taskResult)
                                        } catch (e: CancellationException) {
                                            logger.warn("Cancelled execute task.", e)
                                            emit(
                                                taskResult {
                                                    this.success = false
                                                    this.attempt = it.attempt
                                                    this.id = it.id
                                                    this.message = e.localizedMessage
                                                }
                                            )
                                            throw e
                                        } catch (e: Exception) {
                                            logger.warn("Failed execute task. name: {} id: {}", it.name, it.id, e)
                                            emit(
                                                taskResult {
                                                    this.success = false
                                                    this.attempt = it.attempt
                                                    this.id = it.id
                                                    this.message = e.localizedMessage
                                                }
                                            )
                                        } finally {
                                            logger.debug(" Task name: {} id: {}", it.name, it.id)
                                            processing.update { it - 1 }
                                            concurrent.update {
                                                if (it < 64) {
                                                    it + 1
                                                } else {
                                                    64
                                                }
                                            }
                                        }
                                    }.flowOn(Dispatchers.Default).collect()
                            }
                        )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.warn("Consumer error", e)
                }

                delay(1000)
            }
        }
    }

    private suspend fun FlowCollector<Task.ReadyRequest>.requestTask() {
        while (coroutineScope.isActive) {
            val andSet = concurrent.getAndUpdate { 0 }

            if (andSet != 0) {
                logger.debug("Request {} tasks.", andSet)
                try {
                    emit(
                        readyRequest {
                            this.consumerId = this@Consumer.consumerId
                            this.numberOfConcurrent = andSet
                        }
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.warn("Failed request task.", e)
                }
                continue
            }
            delay(100)

            concurrent.update {
                ((64 - it) - processing.value).coerceIn(0, 64 - max(0, processing.value))
            }
        }
    }

    /**
     * タスクの受付を停止します
     *
     */
    fun stop() {
        logger.info("Stop Consumer. consumerID: {}", consumerId)
        coroutineScope.cancel()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Consumer::class.java)
    }
}
