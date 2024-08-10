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

package dev.usbharu.owl.broker.service

import dev.usbharu.owl.broker.domain.exception.repository.RecordNotFoundException
import dev.usbharu.owl.broker.domain.model.consumer.ConsumerRepository
import dev.usbharu.owl.broker.domain.model.queuedtask.QueuedTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import java.util.*
interface AssignQueuedTaskDecider {
    fun findAssignableQueue(consumerId: UUID, numberOfConcurrent: Int): Flow<QueuedTask>
}
class AssignQueuedTaskDeciderImpl(
    private val consumerRepository: ConsumerRepository,
    private val queueStore: QueueStore
) : AssignQueuedTaskDecider {
    override fun findAssignableQueue(consumerId: UUID, numberOfConcurrent: Int): Flow<QueuedTask> {
        return flow {
            val consumer = consumerRepository.findById(consumerId)
                ?: throw RecordNotFoundException("Consumer not found. id: $consumerId")
            emitAll(
                queueStore.findByTaskNameInAndIsActiveIsTrueAndOrderByPriority(
                    consumer.tasks,
                    numberOfConcurrent
                ).take(numberOfConcurrent)
            )
        }

    }

}