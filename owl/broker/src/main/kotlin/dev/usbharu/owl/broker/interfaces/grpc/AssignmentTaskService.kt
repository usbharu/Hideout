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


import dev.usbharu.owl.AssignmentTaskServiceGrpcKt
import dev.usbharu.owl.Task
import dev.usbharu.owl.broker.external.toTimestamp
import dev.usbharu.owl.broker.external.toUUID
import dev.usbharu.owl.broker.service.QueuedTaskAssigner
import dev.usbharu.owl.common.property.PropertySerializeUtils
import dev.usbharu.owl.common.property.PropertySerializerFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Singleton
class AssignmentTaskService(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    private val queuedTaskAssigner: QueuedTaskAssigner,
    private val propertySerializerFactory: PropertySerializerFactory
) :
    AssignmentTaskServiceGrpcKt.AssignmentTaskServiceCoroutineImplBase(coroutineContext) {

    override fun ready(requests: Flow<Task.ReadyRequest>): Flow<Task.TaskRequest> {
        return requests
            .flatMapMerge {
                queuedTaskAssigner.ready(it.consumerId.toUUID(), it.numberOfConcurrent)
            }
            .map {
                Task.TaskRequest
                    .newBuilder()
                    .setName(it.task.name)
                    .setId(it.task.id.toUUID())
                    .setAttempt(it.attempt)
                    .setQueuedAt(it.queuedAt.toTimestamp())
                    .putAllProperties(PropertySerializeUtils.serialize(propertySerializerFactory, it.task.properties))
                    .build()
            }
    }
}