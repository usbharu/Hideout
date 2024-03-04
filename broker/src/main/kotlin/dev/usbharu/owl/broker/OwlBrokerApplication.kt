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
import dev.usbharu.owl.broker.service.TaskManagementService
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton

@Singleton
class OwlBrokerApplication(
    private val assignmentTaskService: AssignmentTaskService,
    private val definitionTaskService: DefinitionTaskService,
    private val producerService: ProducerService,
    private val subscribeTaskService: SubscribeTaskService,
    private val taskPublishService: TaskPublishService,
    private val taskManagementService: TaskManagementService
) {

    private lateinit var server: Server

    fun start(port: Int,coroutineScope: CoroutineScope = GlobalScope):Job {
        server = ServerBuilder.forPort(port)
            .addService(assignmentTaskService)
            .addService(definitionTaskService)
            .addService(producerService)
            .addService(subscribeTaskService)
            .addService(taskPublishService)
            .build()

        server.start()
        Runtime.getRuntime().addShutdownHook(
            Thread {
                server.shutdown()
            }
        )

        return coroutineScope.launch {
            taskManagementService.startManagement()
        }
    }

    fun stop() {
        server.shutdown()
    }

}