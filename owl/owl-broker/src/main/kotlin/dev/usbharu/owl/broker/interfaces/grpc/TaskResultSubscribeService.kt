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

package dev.usbharu.owl.broker.interfaces.grpc

import dev.usbharu.owl.broker.external.toUUID
import dev.usbharu.owl.broker.service.TaskManagementService
import dev.usbharu.owl.common.property.PropertySerializeUtils
import dev.usbharu.owl.common.property.PropertySerializerFactory
import dev.usbharu.owl.generated.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class TaskResultSubscribeService(
    private val taskManagementService: TaskManagementService,
    private val propertySerializerFactory: PropertySerializerFactory,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) :
    TaskResultSubscribeServiceGrpcKt.TaskResultSubscribeServiceCoroutineImplBase(coroutineContext) {
    override fun subscribe(request: Uuid.UUID): Flow<TaskResultProducer.TaskResults> {
        return taskManagementService
            .subscribeResult(request.toUUID())
            .map {
                taskResults {
                    id = it.id.toUUID()
                    name = it.name
                    attempt = it.attempt
                    success = it.success
                    results.addAll(
                        it.results.map {
                            taskResult {
                                id = it.taskId.toUUID()
                                success = it.success
                                attempt = it.attempt
                                result.putAll(PropertySerializeUtils.serialize(propertySerializerFactory, it.result))
                                message = it.message
                            }
                        }
                    )
                }
            }
    }
}
