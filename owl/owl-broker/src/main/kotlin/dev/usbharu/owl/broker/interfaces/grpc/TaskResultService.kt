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
import dev.usbharu.owl.TaskResultOuterClass
import dev.usbharu.owl.TaskResultServiceGrpcKt
import dev.usbharu.owl.broker.domain.model.taskresult.TaskResult
import dev.usbharu.owl.broker.external.toUUID
import dev.usbharu.owl.broker.service.TaskManagementService
import dev.usbharu.owl.common.property.PropertySerializeUtils
import dev.usbharu.owl.common.property.PropertySerializerFactory
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Singleton
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Singleton
class TaskResultService(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    private val taskManagementService: TaskManagementService,
    private val propertySerializerFactory: PropertySerializerFactory
) :
    TaskResultServiceGrpcKt.TaskResultServiceCoroutineImplBase(coroutineContext) {
    override suspend fun tasKResult(requests: Flow<TaskResultOuterClass.TaskResult>): Empty {
        try {
            requests.onEach {
                taskManagementService.queueProcessed(
                    TaskResult(
                        id = UUID.randomUUID(),
                        taskId = it.id.toUUID(),
                        success = it.success,
                        attempt = it.attempt,
                        result = PropertySerializeUtils.deserialize(propertySerializerFactory, it.resultMap),
                        message = it.message
                    )
                )
            }.collect()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn("Error while executing task results", e)
            throw StatusException(Status.INTERNAL.withDescription("Error while executing task results").withCause(e))
        }
        return Empty.getDefaultInstance()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TaskResultService::class.java)
    }
}