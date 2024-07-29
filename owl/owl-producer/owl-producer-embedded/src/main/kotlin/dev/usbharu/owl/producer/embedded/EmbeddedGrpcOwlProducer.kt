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
import dev.usbharu.owl.broker.mainModule
import dev.usbharu.owl.common.retry.RetryPolicyFactory
import dev.usbharu.owl.common.task.PublishedTask
import dev.usbharu.owl.common.task.Task
import dev.usbharu.owl.common.task.TaskDefinition
import dev.usbharu.owl.producer.api.OwlProducer
import org.koin.core.Koin
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

class EmbeddedGrpcOwlProducer(
    private val config: EmbeddedGrpcOwlProducerConfig,
) : OwlProducer {

    private lateinit var application: Koin

    override suspend fun start() {
        application = startKoin {
            printLogger()

            val module = module {
                single<RetryPolicyFactory> {
                    config.retryPolicyFactory
                }
            }
            modules(mainModule, module, config.moduleContext.module())
        }.koin

        application.get<OwlBrokerApplication>().start(config.port.toInt())
    }

    override suspend fun <T : Task> registerTask(taskDefinition: TaskDefinition<T>) {
        config.owlProducer.registerTask(taskDefinition)
    }

    override suspend fun <T : Task> publishTask(task: T): PublishedTask<T> {
        return config.owlProducer.publishTask(task)
    }

    override suspend fun stop() {
        config.owlProducer.stop()
    }
}