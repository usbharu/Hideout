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
import dev.usbharu.owl.broker.domain.model.taskresult.TaskResult
import dev.usbharu.owl.broker.domain.model.taskresult.TaskResultRepository
import dev.usbharu.owl.common.retry.RetryPolicyFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

interface TaskManagementService {

    suspend fun startManagement(coroutineScope: CoroutineScope)
    fun findAssignableTask(consumerId: UUID, numberOfConcurrent: Int): Flow<QueuedTask>

    suspend fun queueProcessed(taskResult: TaskResult)

    fun subscribeResult(producerId: UUID): Flow<TaskResults>
}

class TaskManagementServiceImpl(
    private val taskScanner: TaskScanner,
    private val queueStore: QueueStore,
    private val taskDefinitionRepository: TaskDefinitionRepository,
    private val assignQueuedTaskDecider: AssignQueuedTaskDecider,
    private val retryPolicyFactory: RetryPolicyFactory,
    private val taskRepository: TaskRepository,
    private val queueScanner: QueueScanner,
    private val taskResultRepository: TaskResultRepository
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
        val definedTask = taskDefinitionRepository.findByName(task.name)
            ?: throw TaskNotRegisterException("Task ${task.name} not definition.")

        val queuedTask = QueuedTask(
            attempt = task.attempt + 1,
            queuedAt = Instant.now(),
            task = task,
            priority = definedTask.priority,
            isActive = true,
            timeoutAt = null,
            assignedConsumer = null,
            assignedAt = null
        )

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

    override suspend fun queueProcessed(taskResult: TaskResult) {
        val task = taskRepository.findById(taskResult.taskId)
            ?: throw RecordNotFoundException("Task not found. id: ${taskResult.taskId}")

        val taskDefinition = taskDefinitionRepository.findByName(task.name)
            ?: throw TaskNotRegisterException("Task ${task.name} not definition.")

        val completedAt = if (taskResult.success) {
            Instant.now()
        } else if (taskResult.attempt >= taskDefinition.maxRetry) {
            Instant.now()
        } else {
            null
        }

        taskResultRepository.save(taskResult)

        taskRepository.findByIdAndUpdate(
            taskResult.taskId,
            task.copy(completedAt = completedAt, attempt = taskResult.attempt)
        )
    }

    override fun subscribeResult(producerId: UUID): Flow<TaskResults> {
        return flow {
            while (currentCoroutineContext().isActive) {
                taskRepository
                    .findByPublishProducerIdAndCompletedAtIsNotNull(producerId)
                    .onEach {
                        val results = taskResultRepository.findByTaskId(it.id).toList()
                        emit(
                            TaskResults(
                                it.name,
                                it.id,
                                results.any { taskResult -> taskResult.success },
                                it.attempt,
                                results
                            )
                        )
                    }
                delay(500)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TaskManagementServiceImpl::class.java)
    }
}
