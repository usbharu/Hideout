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

import dev.usbharu.owl.broker.domain.model.queuedtask.QueuedTask
import dev.usbharu.owl.broker.domain.model.queuedtask.QueuedTaskRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Singleton

interface QueueStore {
    suspend fun enqueue(queuedTask: QueuedTask)
    suspend fun enqueueAll(queuedTaskList: List<QueuedTask>)

    suspend fun dequeue(queuedTask: QueuedTask)
    suspend fun dequeueAll(queuedTaskList: List<QueuedTask>)
    fun findByTaskNameInAndAssignedConsumerIsNullAndOrderByPriority(tasks: List<String>, limit: Int): Flow<QueuedTask>
}

@Singleton
class QueueStoreImpl(private val queuedTaskRepository: QueuedTaskRepository) : QueueStore {
    override suspend fun enqueue(queuedTask: QueuedTask) {
        queuedTaskRepository.save(queuedTask)
    }

    override suspend fun enqueueAll(queuedTaskList: List<QueuedTask>) {
        queuedTaskList.forEach { enqueue(it) }
    }

    override suspend fun dequeue(queuedTask: QueuedTask) {
        queuedTaskRepository.findByTaskIdAndAssignedConsumerIsNullAndUpdate(queuedTask.task.id, queuedTask)
    }

    override suspend fun dequeueAll(queuedTaskList: List<QueuedTask>) {
        return queuedTaskList.forEach { dequeue(it) }
    }

    override fun findByTaskNameInAndAssignedConsumerIsNullAndOrderByPriority(
        tasks: List<String>,
        limit: Int
    ): Flow<QueuedTask> {
        return queuedTaskRepository.findByTaskNameInAndAssignedConsumerIsNullAndOrderByPriority(tasks, limit)
    }

}