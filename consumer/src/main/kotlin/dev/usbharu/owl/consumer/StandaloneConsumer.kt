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

import dev.usbharu.dev.usbharu.owl.consumer.TaskRunner
import dev.usbharu.owl.AssignmentTaskServiceGrpcKt
import dev.usbharu.owl.SubscribeTaskServiceGrpcKt
import dev.usbharu.owl.TaskResultServiceGrpcKt
import dev.usbharu.owl.common.property.CustomPropertySerializerFactory
import dev.usbharu.owl.common.property.PropertySerializerFactory
import io.grpc.ManagedChannelBuilder
import java.nio.file.Path
import java.util.*

class StandaloneConsumer(
    private val config: StandaloneConsumerConfig,
    private val propertySerializerFactory: PropertySerializerFactory
) {
    constructor(
        path: Path, propertySerializerFactory: PropertySerializerFactory = CustomPropertySerializerFactory(
            emptySet()
        )
    ) : this(StandaloneConsumerConfigLoader.load(path), propertySerializerFactory)

    constructor(string: String) : this(Path.of(string))

    constructor() : this(Path.of("consumer.properties"))

    private val channel = ManagedChannelBuilder.forAddress(config.address, config.port)
        .usePlaintext()
        .build()

    private val subscribeStub = SubscribeTaskServiceGrpcKt.SubscribeTaskServiceCoroutineStub(channel)
    private val assignmentTaskStub = AssignmentTaskServiceGrpcKt.AssignmentTaskServiceCoroutineStub(channel)
    private val taskResultStub = TaskResultServiceGrpcKt.TaskResultServiceCoroutineStub(channel)

    private val taskRunnerMap = ServiceLoader
        .load(TaskRunner::class.java)
        .associateBy { it::class.qualifiedName!! }
        .filterNot { it.key.isBlank() }

    private val consumer = Consumer(
        subscribeStub,
        assignmentTaskStub,
        taskResultStub,
        taskRunnerMap,
        propertySerializerFactory,
        ConsumerConfig(config.concurrency)
    )

    suspend fun init() {
        consumer.init(config.name, config.hostname)
    }

    suspend fun start() {
        consumer.start()
        Runtime.getRuntime().addShutdownHook(Thread {
            consumer.stop()
        })
    }

    fun stop() {
        consumer.stop()
    }

}