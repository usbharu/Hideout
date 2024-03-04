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
import dev.usbharu.owl.broker.domain.model.task.Task
import dev.usbharu.owl.broker.domain.model.task.TaskRepository
import dev.usbharu.owl.broker.domain.model.taskdefinition.TaskDefinitionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Singleton
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*


interface TaskManagementService {

    suspend fun startManagement()
    fun findAssignableTask(consumerId: UUID, numberOfConcurrent: Int): Flow<QueuedTask>
}

@Singleton
class TaskManagementServiceImpl(
    private val taskScanner: TaskScanner,
    private val queueStore: QueueStore,
    private val taskDefinitionRepository: TaskDefinitionRepository,
    private val assignQueuedTaskDecider: AssignQueuedTaskDecider,
    private val retryPolicyFactory: RetryPolicyFactory,
    private val taskRepository: TaskRepository
) : TaskManagementService {

    private var flow:Flow<Task> = flowOf()
    override suspend fun startManagement() {
        flow = taskScanner.startScan()

            flow.onEach {
                enqueueTask(it)
            }.collect()

    }


    override fun findAssignableTask(consumerId: UUID, numberOfConcurrent: Int): Flow<QueuedTask> {
        return assignQueuedTaskDecider.findAssignableQueue(consumerId, numberOfConcurrent)
    }

    private suspend fun enqueueTask(task: Task):QueuedTask{

        val queuedTask = QueuedTask(
            task.attempt + 1,
            Instant.now(),
            task,
            null,
            null
        )

        val copy = task.copy(
            nextRetry = retryPolicyFactory.factory(taskDefinitionRepository.findByName(task.name)?.retryPolicy.orEmpty())
                .nextRetry(Instant.now(), task.attempt)
        )

        taskRepository.save(copy)

        queueStore.enqueue(queuedTask)
        logger.debug("Enqueue Task. {} {}", task.name, task.id)
        return queuedTask
    }

    companion object{
        private val logger = LoggerFactory.getLogger(TaskManagementServiceImpl::class.java)
    }
}