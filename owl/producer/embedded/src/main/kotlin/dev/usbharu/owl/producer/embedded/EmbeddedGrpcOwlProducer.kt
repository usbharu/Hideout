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

import dev.usbharu.owl.broker.ModuleContext
import dev.usbharu.owl.broker.OwlBrokerApplication
import dev.usbharu.owl.broker.service.RetryPolicyFactory
import dev.usbharu.owl.common.task.PublishedTask
import dev.usbharu.owl.common.task.Task
import dev.usbharu.owl.common.task.TaskDefinition
import dev.usbharu.owl.producer.api.OwlProducer
import org.koin.core.Koin
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule

class EmbeddedGrpcOwlProducer(
    private val moduleContext: ModuleContext,
    private val retryPolicyFactory: RetryPolicyFactory,
    private val port: Int,
    private val owlProducer: OwlProducer,
) : OwlProducer {

    private lateinit var application: Koin

    override suspend fun start() {
        application = startKoin {
            printLogger()

            val module = module {
                single<RetryPolicyFactory> {
                    retryPolicyFactory
                }
            }
            modules(module, defaultModule, moduleContext.module())
        }.koin

        application.get<OwlBrokerApplication>().start(port)
    }

    override suspend fun <T : Task> registerTask(taskDefinition: TaskDefinition<T>) {
        owlProducer.registerTask(taskDefinition)
    }

    override suspend fun <T : Task> publishTask(task: T): PublishedTask<T> {
        return owlProducer.publishTask(task)
    }
}