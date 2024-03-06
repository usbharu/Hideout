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

import dev.usbharu.owl.broker.domain.exception.service.QueueCannotDequeueException
import dev.usbharu.owl.broker.domain.model.queuedtask.QueuedTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Singleton
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

interface QueuedTaskAssigner {
    fun ready(consumerId: UUID, numberOfConcurrent: Int): Flow<QueuedTask>
}

@Singleton
class QueuedTaskAssignerImpl(
    private val taskManagementService: TaskManagementService,
    private val queueStore: QueueStore
) : QueuedTaskAssigner {
    override fun ready(consumerId: UUID, numberOfConcurrent: Int): Flow<QueuedTask> {
        return flow {
            taskManagementService.findAssignableTask(consumerId, numberOfConcurrent)
                .onEach {
                    val assignTask = assignTask(it, consumerId)

                    if (assignTask != null) {
                        emit(assignTask)
                    }
                }
                .collect()
        }
    }

    private suspend fun assignTask(queuedTask: QueuedTask, consumerId: UUID): QueuedTask? {
        return try {

            val assignedTaskQueue =
                queuedTask.copy(assignedConsumer = consumerId, assignedAt = Instant.now(), isActive = false)
            logger.trace(
                "Try assign task: {} id: {} consumer: {}",
                queuedTask.task.name,
                queuedTask.task.id,
                consumerId
            )

            queueStore.dequeue(assignedTaskQueue)

            logger.debug(
                "Assign Task. name: {} id: {} attempt: {} consumer: {}",
                queuedTask.task.name,
                queuedTask.task.id,
                queuedTask.attempt,
                queuedTask.assignedConsumer
            )
            assignedTaskQueue
        } catch (e: QueueCannotDequeueException) {
            logger.debug("Failed dequeue queue", e)
            return null
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(QueuedTaskAssignerImpl::class.java)
    }
}