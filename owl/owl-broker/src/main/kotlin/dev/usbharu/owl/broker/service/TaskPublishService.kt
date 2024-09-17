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

import dev.usbharu.owl.broker.domain.exception.service.TaskNotRegisterException
import dev.usbharu.owl.broker.domain.model.task.Task
import dev.usbharu.owl.broker.domain.model.task.TaskRepository
import dev.usbharu.owl.broker.domain.model.taskdefinition.TaskDefinitionRepository
import dev.usbharu.owl.common.property.PropertyValue
import dev.usbharu.owl.common.retry.RetryPolicyFactory
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

interface TaskPublishService {
    suspend fun publishTask(publishTask: PublishTask): PublishedTask
    suspend fun publishTasks(list: List<PublishTask>): List<PublishedTask>
}

data class PublishTask(
    val name: String,
    val producerId: UUID,
    val properties: Map<String, PropertyValue<*>>
)

data class PublishedTask(
    val name: String,
    val id: UUID
)

class TaskPublishServiceImpl(
    private val taskRepository: TaskRepository,
    private val taskDefinitionRepository: TaskDefinitionRepository,
    private val retryPolicyFactory: RetryPolicyFactory
) : TaskPublishService {
    override suspend fun publishTask(publishTask: PublishTask): PublishedTask {
        val id = UUID.randomUUID()

        val definition = taskDefinitionRepository.findByName(publishTask.name)
            ?: throw TaskNotRegisterException("Task ${publishTask.name} not definition.")

        val published = Instant.now()
        val nextRetry = retryPolicyFactory.factory(definition.retryPolicy).nextRetry(published, 0)

        val task = Task(
            name = publishTask.name,
            id = id,
            publishProducerId = publishTask.producerId,
            publishedAt = published,
            completedAt = null,
            attempt = 0,
            properties = publishTask.properties,
            nextRetry = nextRetry
        )

        taskRepository.save(task)

        logger.debug("Published task #{} name: {}", task.id, task.name)

        return PublishedTask(
            name = publishTask.name,
            id = id
        )
    }

    override suspend fun publishTasks(list: List<PublishTask>): List<PublishedTask> {
        val first = list.first()

        val definition = taskDefinitionRepository.findByName(first.name)
            ?: throw TaskNotRegisterException("Task ${first.name} not definition.")

        val published = Instant.now()

        val nextRetry = retryPolicyFactory.factory(definition.retryPolicy).nextRetry(published, 0)

        val tasks = list.map {
            Task(
                name = it.name,
                id = UUID.randomUUID(),
                publishProducerId = first.producerId,
                publishedAt = published,
                nextRetry = nextRetry,
                completedAt = null,
                attempt = 0,
                properties = it.properties
            )
        }

        taskRepository.saveAll(tasks)

        logger.debug("Published {} tasks. name: {}", tasks.size, first.name)

        return tasks.map { PublishedTask(it.name, it.id) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TaskPublishServiceImpl::class.java)
    }
}
