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

package dev.usbharu.owl.producer.defaultimpl

import com.google.protobuf.timestamp
import dev.usbharu.owl.common.property.PropertySerializeUtils
import dev.usbharu.owl.common.task.PublishedTask
import dev.usbharu.owl.common.task.Task
import dev.usbharu.owl.common.task.TaskDefinition
import dev.usbharu.owl.generated.*
import dev.usbharu.owl.generated.Uuid.UUID
import dev.usbharu.owl.producer.api.OwlProducer
import java.time.Instant

class DefaultOwlProducer(private val defaultOwlProducerConfig: DefaultOwlProducerConfig) : OwlProducer {

    lateinit var producerId: UUID
    lateinit var producerServiceCoroutineStub: ProducerServiceGrpcKt.ProducerServiceCoroutineStub
    lateinit var defineTaskServiceCoroutineStub: DefinitionTaskServiceGrpcKt.DefinitionTaskServiceCoroutineStub
    lateinit var taskPublishServiceCoroutineStub: TaskPublishServiceGrpcKt.TaskPublishServiceCoroutineStub
    val map = mutableMapOf<Class<*>, TaskDefinition<*>>()
    override suspend fun start() {
        producerServiceCoroutineStub =
            ProducerServiceGrpcKt.ProducerServiceCoroutineStub(defaultOwlProducerConfig.channel)
        producerId = producerServiceCoroutineStub.registerProducer(
            producer {
                this.name = defaultOwlProducerConfig.name
                this.hostname = defaultOwlProducerConfig.hostname
            }
        ).id

        defineTaskServiceCoroutineStub =
            DefinitionTaskServiceGrpcKt.DefinitionTaskServiceCoroutineStub(defaultOwlProducerConfig.channel)

        taskPublishServiceCoroutineStub =
            TaskPublishServiceGrpcKt.TaskPublishServiceCoroutineStub(defaultOwlProducerConfig.channel)
    }

    override suspend fun <T : Task> registerTask(taskDefinition: TaskDefinition<T>) {
        defineTaskServiceCoroutineStub.register(
            taskDefinition {
                this.producerId = this@DefaultOwlProducer.producerId
                this.name = taskDefinition.name
                this.maxRetry = taskDefinition.maxRetry
                this.priority = taskDefinition.priority
                this.retryPolicy = taskDefinition.retryPolicy
                this.timeoutMilli = taskDefinition.timeoutMilli
                this.propertyDefinitionHash = taskDefinition.propertyDefinition.hash()
            }
        )
    }

    override suspend fun <T : Task> publishTask(task: T): PublishedTask<T> {
        val taskDefinition = map.getValue(task::class.java) as TaskDefinition<T>
        val properties = PropertySerializeUtils.serialize(
            defaultOwlProducerConfig.propertySerializerFactory,
            taskDefinition.serialize(task)
        )
        val now = Instant.now()
        val publishTask = taskPublishServiceCoroutineStub.publishTask(
            dev.usbharu.owl.generated.publishTask {
                this.producerId = this@DefaultOwlProducer.producerId

                this.publishedAt = timestamp {
                    this.seconds = now.epochSecond
                    this.nanos = now.nano
                }
                this.name = taskDefinition.name
                this.properties.putAll(properties)
            }
        )

        return PublishedTask(
            task,
            java.util.UUID(publishTask.id.mostSignificantUuidBits, publishTask.id.leastSignificantUuidBits),
            now
        )
    }

    override suspend fun stop() {
        defaultOwlProducerConfig.channel.shutdownNow()
    }
}
