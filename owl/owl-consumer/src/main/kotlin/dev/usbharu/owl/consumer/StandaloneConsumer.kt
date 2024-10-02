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

import dev.usbharu.owl.common.property.CustomPropertySerializerFactory
import dev.usbharu.owl.common.property.PropertySerializerFactory
import dev.usbharu.owl.generated.AssignmentTaskServiceGrpcKt
import dev.usbharu.owl.generated.SubscribeTaskServiceGrpcKt
import dev.usbharu.owl.generated.TaskResultServiceGrpcKt
import io.grpc.ManagedChannelBuilder
import java.nio.file.Path

/**
 * 単独で起動できるConsumer
 *
 * @property config Consumerの起動構成
 * @property propertySerializerFactory [dev.usbharu.owl.common.property.PropertyValue]のシリアライザーのファクトリ
 */
class StandaloneConsumer(
    private val config: StandaloneConsumerConfig,
    private val propertySerializerFactory: PropertySerializerFactory,
    taskRunnerLoader: TaskRunnerLoader,
) {
    constructor(
        path: Path,
        propertySerializerFactory: PropertySerializerFactory = CustomPropertySerializerFactory(
            emptySet()
        ),
        taskRunnerLoader: TaskRunnerLoader = ServiceLoaderTaskRunnerLoader(),
    ) : this(StandaloneConsumerConfigLoader.load(path), propertySerializerFactory, taskRunnerLoader)

    constructor(
        string: String,
        propertySerializerFactory: PropertySerializerFactory = CustomPropertySerializerFactory(emptySet()),
        taskRunnerLoader: TaskRunnerLoader = ServiceLoaderTaskRunnerLoader(),
    ) : this(Path.of(string), propertySerializerFactory, taskRunnerLoader)

    constructor(
        propertySerializerFactory: PropertySerializerFactory = CustomPropertySerializerFactory(emptySet()),
        taskRunnerLoader: TaskRunnerLoader = ServiceLoaderTaskRunnerLoader(),
    ) : this(
        Path.of(StandaloneConsumer::class.java.getClassLoader().getResource("consumer.properties").toURI()),
        propertySerializerFactory,
        taskRunnerLoader
    )

    private val channel = ManagedChannelBuilder.forAddress(config.address, config.port)
        .usePlaintext()
        .build()

    private val subscribeStub = SubscribeTaskServiceGrpcKt.SubscribeTaskServiceCoroutineStub(channel)
    private val assignmentTaskStub = AssignmentTaskServiceGrpcKt.AssignmentTaskServiceCoroutineStub(channel)
    private val taskResultStub = TaskResultServiceGrpcKt.TaskResultServiceCoroutineStub(channel)

    private val consumer = Consumer(
        subscribeTaskStub = subscribeStub,
        assignmentTaskStub = assignmentTaskStub,
        taskResultStub = taskResultStub,
        taskRunnerLoader = taskRunnerLoader,
        propertySerializerFactory = propertySerializerFactory,
        consumerConfig = ConsumerConfig(config.concurrency),
    )

    /**
     * Consumerを初期化します
     *
     */
    suspend fun init() {
        consumer.init(config.name, config.hostname)
    }

    /**
     * Consumerのワーカーを起動し、タスクの受付を開始します。
     *
     * シャットダウンフックに[stop]が登録されます。
     */
    suspend fun start() {
        consumer.start()
        Runtime.getRuntime().addShutdownHook(
            Thread {
                consumer.stop()
            }
        )
    }

    /**
     * Consumerを停止します
     *
     */
    fun stop() {
        consumer.stop()
    }
}
