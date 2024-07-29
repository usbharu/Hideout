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

package dev.usbharu.owl.broker

import dev.usbharu.owl.broker.interfaces.grpc.*
import dev.usbharu.owl.broker.service.*
import dev.usbharu.owl.broker.service.ProducerService
import dev.usbharu.owl.broker.service.TaskPublishService
import dev.usbharu.owl.common.property.PropertySerializerFactory
import dev.usbharu.owl.common.retry.DefaultRetryPolicyFactory
import dev.usbharu.owl.common.retry.ExponentialRetryPolicy
import dev.usbharu.owl.common.retry.RetryPolicyFactory
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import java.util.*

val logger = LoggerFactory.getLogger("MAIN")

fun main() {
    val moduleContexts = ServiceLoader.load(ModuleContext::class.java)

    val moduleContext = moduleContexts.first()

    logger.info("Use module name: {}", moduleContext)


    val koin = startKoin {
        printLogger()

        val module = module {
            single<RetryPolicyFactory> {
                DefaultRetryPolicyFactory(mapOf("" to ExponentialRetryPolicy()))
            }
            single<AssignQueuedTaskDecider> {
                AssignQueuedTaskDeciderImpl(get(), get())
            }
            single<TaskScanner> { TaskScannerImpl(get()) }
            single<TaskPublishService> { TaskPublishServiceImpl(get(), get(), get()) }
            single<TaskManagementService> {
                TaskManagementServiceImpl(
                    taskScanner = get(),
                    queueStore = get(),
                    taskDefinitionRepository = get(),
                    assignQueuedTaskDecider = get(),
                    retryPolicyFactory = get(),
                    taskRepository = get(),
                    queueScanner = get(),
                    taskResultRepository = get()
                )
            }
            single<RegisterTaskService> { RegisterTaskServiceImpl(get()) }
            single<QueueStore> { QueueStoreImpl(get()) }
            single<QueueScanner> { QueueScannerImpl(get()) }
            single<QueuedTaskAssigner> { QueuedTaskAssignerImpl(get(), get()) }
            single<ProducerService> { ProducerServiceImpl(get()) }
            single<PropertySerializerFactory> { DefaultPropertySerializerFactory() }
            single<ConsumerService> { ConsumerServiceImpl(get()) }
            single {
                OwlBrokerApplication(
                    assignmentTaskService = get(),
                    definitionTaskService = get(),
                    producerService = get(),
                    subscribeTaskService = get(),
                    taskPublishService = get(),
                    taskManagementService = get(),
                    taskResultSubscribeService = get(),
                    taskResultService = get()
                )
            }
            single { AssignmentTaskService(queuedTaskAssigner = get(), propertySerializerFactory = get()) }
            single { DefinitionTaskService(registerTaskService = get()) }
            single { dev.usbharu.owl.broker.interfaces.grpc.ProducerService(producerService = get()) }
            single { SubscribeTaskService(consumerService = get()) }
            single {
                dev.usbharu.owl.broker.interfaces.grpc.TaskPublishService(
                    taskPublishService = get(),
                    propertySerializerFactory = get()
                )
            }
            single { TaskResultService(taskManagementService = get(), propertySerializerFactory = get()) }
            single { TaskResultSubscribeService(taskManagementService = get(), propertySerializerFactory = get()) }
        }
        modules(module, moduleContext.module())
    }

    val application = koin.koin.get<OwlBrokerApplication>()

    runBlocking {
        application.start(50051).join()
    }
}