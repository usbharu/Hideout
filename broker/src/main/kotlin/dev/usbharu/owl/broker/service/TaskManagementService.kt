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
import dev.usbharu.owl.broker.domain.exception.service.TaskNotRegisterException
import dev.usbharu.owl.broker.domain.model.queuedtask.QueuedTask
import dev.usbharu.owl.broker.domain.model.task.Task
import dev.usbharu.owl.broker.domain.model.task.TaskRepository
import dev.usbharu.owl.broker.domain.model.taskdefinition.TaskDefinitionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*


interface TaskManagementService {

    suspend fun startManagement(coroutineScope: CoroutineScope)
    fun findAssignableTask(consumerId: UUID, numberOfConcurrent: Int): Flow<QueuedTask>
}

@Singleton
class TaskManagementServiceImpl(
    private val taskScanner: TaskScanner,
    private val queueStore: QueueStore,
    private val taskDefinitionRepository: TaskDefinitionRepository,
    private val assignQueuedTaskDecider: AssignQueuedTaskDecider,
    private val retryPolicyFactory: RetryPolicyFactory,
    private val taskRepository: TaskRepository,
    private val queueScanner: QueueScanner
) : TaskManagementService {

    private var taskFlow: Flow<Task> = flowOf()
    private var queueFlow: Flow<QueuedTask> = flowOf()
    override suspend fun startManagement(coroutineScope: CoroutineScope) {
        taskFlow = taskScanner.startScan()
        queueFlow = queueScanner.startScan()

        coroutineScope {
            listOf(
                launch {
                    taskFlow.onEach {
                        enqueueTask(it)
                    }.collect()
                },
                launch {
                    queueFlow.onEach {
                        timeoutQueue(it)
                    }.collect()
                }
            ).joinAll()
        }
    }


    override fun findAssignableTask(consumerId: UUID, numberOfConcurrent: Int): Flow<QueuedTask> {
        return assignQueuedTaskDecider.findAssignableQueue(consumerId, numberOfConcurrent)
    }

    private suspend fun enqueueTask(task: Task): QueuedTask {

        val queuedTask = QueuedTask(
            task.attempt + 1,
            Instant.now(),
            task,
            isActive = true,
            timeoutAt = null,
            null,
            null
        )

        val definedTask = taskDefinitionRepository.findByName(task.name)
            ?: throw TaskNotRegisterException("Task ${task.name} not definition.")
        val copy = task.copy(
            nextRetry = retryPolicyFactory.factory(definedTask.retryPolicy)
                .nextRetry(Instant.now(), queuedTask.attempt)
        )

        taskRepository.save(copy)

        queueStore.enqueue(queuedTask)
        logger.debug("Enqueue Task. name: {} id: {} attempt: {}", task.name, task.id, queuedTask.attempt)
        return queuedTask
    }

    private suspend fun timeoutQueue(queuedTask: QueuedTask) {
        val timeoutQueue = queuedTask.copy(isActive = false, timeoutAt = Instant.now())

        queueStore.dequeue(timeoutQueue)


        val task = taskRepository.findById(timeoutQueue.task.id)
            ?: throw RecordNotFoundException("Task not found. id: ${timeoutQueue.task.id}")
        val copy = task.copy(attempt = timeoutQueue.attempt)

        logger.warn(
            "Queue timed out. name: {} id: {} attempt: {}",
            timeoutQueue.task.name,
            timeoutQueue.task.id,
            timeoutQueue.attempt
        )
        taskRepository.save(copy)
    }


    companion object {
        private val logger = LoggerFactory.getLogger(TaskManagementServiceImpl::class.java)
    }
}