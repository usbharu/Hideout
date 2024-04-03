package dev.usbharu

import dev.usbharu.dev.usbharu.owl.consumer.TaskRequest
import dev.usbharu.dev.usbharu.owl.consumer.TaskRunner
import dev.usbharu.owl.*
import dev.usbharu.owl.common.property.CustomPropertySerializerFactory
import dev.usbharu.owl.common.property.PropertySerializeUtils
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*
import kotlin.math.max

suspend fun main() {

    val channel = ManagedChannelBuilder.forAddress(
        "localhost", 50051
    ).build()
    val subscribeTaskServiceCoroutineStub = SubscribeTaskServiceGrpcKt.SubscribeTaskServiceCoroutineStub(channel)

    val assignmentTaskServiceCoroutineStub = AssignmentTaskServiceGrpcKt.AssignmentTaskServiceCoroutineStub(
        channel
    )

    val taskResultServiceCoroutineStub = TaskResultServiceGrpcKt.TaskResultServiceCoroutineStub(channel)

    val subscribeTask = subscribeTaskServiceCoroutineStub.subscribeTask(subscribeTaskRequest {
        this.name = ""
        this.hostname = ""
        this.tasks.addAll(listOf())
    })


    val map = mapOf<String, TaskRunner>()

    val propertySerializerFactory = CustomPropertySerializerFactory(setOf())

    val concurrent = MutableStateFlow(64)
    val processing = MutableStateFlow(0)


    coroutineScope {
        launch(Dispatchers.Default) {
            taskResultServiceCoroutineStub.tasKResult(flow {
                assignmentTaskServiceCoroutineStub
                    .ready(
                        flow {
                            while (this@coroutineScope.isActive) {

                                val andSet = concurrent.getAndUpdate {
                                    0
                                }


                                if (andSet != 0) {
                                    emit(readyRequest {
                                        this.consumerId = subscribeTask.id
                                        this.numberOfConcurrent = andSet
                                    })
                                    continue
                                }
                                delay(100)

                                concurrent.update {
                                    ((64 - it) - processing.value).coerceIn(0, 64 - max(0, processing.value))
                                }
                            }
                        }
                    )
                    .onEach {

                        processing.update { it + 1 }

                        try {

                            val taskResult = map[it.name]?.run(
                                TaskRequest(
                                    it.name,
                                    UUID(it.id.mostSignificantUuidBits, it.id.leastSignificantUuidBits),
                                    it.attempt,
                                    Instant.ofEpochSecond(it.queuedAt.seconds, it.queuedAt.nanos.toLong()),
                                    PropertySerializeUtils.deserialize(propertySerializerFactory, it.propertiesMap)
                                )
                            )

                            if (taskResult == null) {
                                throw Exception()
                            }

                            emit(taskResult {
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
                            })

                        } catch (e: Exception) {
                            emit(taskResult {
                                this.success = false
                                this.attempt = it.attempt
                                this.id = it.id
                                this.message = e.localizedMessage
                            })
                        } finally {
                            processing.update { it - 1 }
                            concurrent.update {
                                if (it < 64) {
                                    it + 1
                                } else {
                                    64
                                }
                            }
                        }
                    }
                    .flowOn(Dispatchers.Default)
                    .collect()
            })
        }
    }
}