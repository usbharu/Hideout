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

package dev.usbharu.owl.producer.embedded

import dev.usbharu.owl.broker.OwlBrokerApplication
import dev.usbharu.owl.broker.domain.exception.InvalidRepositoryException
import dev.usbharu.owl.broker.domain.model.producer.ProducerRepository
import dev.usbharu.owl.broker.service.*
import dev.usbharu.owl.common.retry.RetryPolicyFactory
import dev.usbharu.owl.common.task.PublishedTask
import dev.usbharu.owl.common.task.Task
import dev.usbharu.owl.common.task.TaskDefinition
import dev.usbharu.owl.producer.api.OwlProducer
import org.koin.core.Koin
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import java.time.Instant
import java.util.*
import dev.usbharu.owl.broker.domain.model.taskdefinition.TaskDefinition as BrokerTaskDefinition

class EmbeddedOwlProducer(
    private val embeddedOwlProducerConfig: EmbeddedOwlProducerConfig,
) : OwlProducer {

    private lateinit var producerId: UUID

    private lateinit var application: Koin

    private val taskMap: MutableMap<Class<*>, TaskDefinition<*>> = mutableMapOf()

    override suspend fun start() {
        application = startKoin {
            printLogger()

            val module = module {
                single<RetryPolicyFactory> {
                    embeddedOwlProducerConfig.retryPolicyFactory
                }
            }
            modules(module, defaultModule, embeddedOwlProducerConfig.moduleContext.module())
        }.koin

        application.getOrNull<ProducerRepository>()
            ?: throw InvalidRepositoryException("Repository not found. Install owl-broker-mongodb, etc. on the classpath")

        val producerService = application.get<ProducerService>()

        producerId = producerService.registerProducer(
            RegisterProducerRequest(
                embeddedOwlProducerConfig.name,
                embeddedOwlProducerConfig.name
            )
        )

        application.get<OwlBrokerApplication>().start(embeddedOwlProducerConfig.port.toInt())
    }

    override suspend fun <T : Task> registerTask(taskDefinition: TaskDefinition<T>) {
        application.get<RegisterTaskService>()
            .registerTask(
                BrokerTaskDefinition(
                    name = taskDefinition.name,
                    priority = taskDefinition.priority,
                    maxRetry = taskDefinition.maxRetry,
                    timeoutMilli = taskDefinition.timeoutMilli,
                    propertyDefinitionHash = taskDefinition.propertyDefinition.hash(),
                    retryPolicy = taskDefinition.retryPolicy
                )
            )

        taskMap[taskDefinition.type] = taskDefinition
    }

    override suspend fun <T : Task> publishTask(task: T): PublishedTask<T> {

        val taskDefinition = taskMap.getValue(task::class.java) as TaskDefinition<T>

        val publishTask = application.get<TaskPublishService>().publishTask(
            PublishTask(
                taskDefinition.name,
                producerId,
                taskDefinition.serialize(task)
            )
        )

        return PublishedTask(
            task,
            publishTask.id,
            Instant.now()
        )
    }
}