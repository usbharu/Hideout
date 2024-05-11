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

import com.google.protobuf.Empty
import dev.usbharu.owl.DefinitionTask
import dev.usbharu.owl.DefinitionTask.TaskDefined
import dev.usbharu.owl.DefinitionTaskServiceGrpcKt.DefinitionTaskServiceCoroutineImplBase
import dev.usbharu.owl.broker.domain.model.taskdefinition.TaskDefinition
import dev.usbharu.owl.broker.service.RegisterTaskService
import org.koin.core.annotation.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Singleton
class DefinitionTaskService(coroutineContext: CoroutineContext = EmptyCoroutineContext,private val registerTaskService: RegisterTaskService) :
    DefinitionTaskServiceCoroutineImplBase(coroutineContext) {
    override suspend fun register(request: DefinitionTask.TaskDefinition): TaskDefined {
        registerTaskService.registerTask(
            TaskDefinition(
                request.name,
                request.priority,
                request.maxRetry,
                request.timeoutMilli,
                request.propertyDefinitionHash,
                request.retryPolicy
            )
        )
        return TaskDefined
            .newBuilder()
            .setTaskId(
                request.name
            )
            .build()
    }

    override suspend fun unregister(request: DefinitionTask.TaskUnregister): Empty {
        registerTaskService.unregisterTask(request.name)
        return Empty.getDefaultInstance()
    }
}