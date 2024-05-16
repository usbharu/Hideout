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

package dev.usbharu.owl.broker.mongodb

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates.set
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import dev.usbharu.owl.broker.domain.model.queuedtask.QueuedTask
import dev.usbharu.owl.broker.domain.model.queuedtask.QueuedTaskRepository
import dev.usbharu.owl.broker.domain.model.task.Task
import dev.usbharu.owl.common.property.PropertySerializeUtils
import dev.usbharu.owl.common.property.PropertySerializerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.bson.BsonType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonRepresentation
import org.koin.core.annotation.Singleton
import java.time.Instant
import java.util.*

@Singleton
class MongodbQueuedTaskRepository(
    private val propertySerializerFactory: PropertySerializerFactory,
    database: MongoDatabase
) : QueuedTaskRepository {

    private val collection = database.getCollection<QueuedTaskMongodb>("queued_task")
    override suspend fun save(queuedTask: QueuedTask): QueuedTask {
        withContext(Dispatchers.IO) {
            collection.replaceOne(
                eq("_id", queuedTask.task.id.toString()), QueuedTaskMongodb.of(propertySerializerFactory, queuedTask),
                ReplaceOptions().upsert(true)
            )
        }
        return queuedTask
    }

    override suspend fun findByTaskIdAndAssignedConsumerIsNullAndUpdate(id: UUID, update: QueuedTask): QueuedTask {
        return withContext(Dispatchers.IO) {

            val findOneAndUpdate = collection.findOneAndUpdate(
                and(
                    eq("_id", id.toString()),
                    eq(QueuedTaskMongodb::isActive.name, true)
                ),
                listOf(
                    set(QueuedTaskMongodb::assignedConsumer.name, update.assignedConsumer?.toString()),
                    set(QueuedTaskMongodb::assignedAt.name, update.assignedAt),
                    set(QueuedTaskMongodb::queuedAt.name, update.queuedAt),
                    set(QueuedTaskMongodb::isActive.name, update.isActive)
                ),
                FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER)
            )
            if (findOneAndUpdate == null) {
                TODO()
            }
            findOneAndUpdate.toQueuedTask(propertySerializerFactory)
        }
    }

    override fun findByTaskNameInAndIsActiveIsTrueAndOrderByPriority(
        tasks: List<String>,
        limit: Int
    ): Flow<QueuedTask> {
        return collection.find<QueuedTaskMongodb>(
            and(
                `in`("task.name", tasks),
                eq(QueuedTaskMongodb::isActive.name, true)
            )
        ).sort(Sorts.descending("priority")).map { it.toQueuedTask(propertySerializerFactory) }.flowOn(Dispatchers.IO)
    }

    override fun findByQueuedAtBeforeAndIsActiveIsTrue(instant: Instant): Flow<QueuedTask> {
        return collection.find(
            and(
                lte(QueuedTaskMongodb::queuedAt.name, instant),
                eq(QueuedTaskMongodb::isActive.name, true)
            )
        )
            .map { it.toQueuedTask(propertySerializerFactory) }.flowOn(Dispatchers.IO)
    }
}

data class QueuedTaskMongodb(
    @BsonId
    @BsonRepresentation(BsonType.STRING)
    val id: String,
    val task: TaskMongodb,
    val attempt: Int,
    val queuedAt: Instant,
    val priority:Int,
    val isActive: Boolean,
    val timeoutAt: Instant?,
    val assignedConsumer: String?,
    val assignedAt: Instant?
) {

    fun toQueuedTask(propertySerializerFactory: PropertySerializerFactory): QueuedTask {
        return QueuedTask(
            attempt = attempt,
            queuedAt = queuedAt,
            task = task.toTask(propertySerializerFactory),
            priority = priority,
            isActive = isActive,
            timeoutAt = timeoutAt,
            assignedConsumer = assignedConsumer?.let { UUID.fromString(it) },
            assignedAt = assignedAt
        )
    }

    data class TaskMongodb(
        val name: String,
        val id: String,
        val publishProducerId: String,
        val publishedAt: Instant,
        val nextRetry: Instant,
        val completedAt: Instant?,
        val attempt: Int,
        val properties: Map<String, String>
    ) {

        fun toTask(propertySerializerFactory: PropertySerializerFactory): Task {
            return Task(
                name = name,
                id = UUID.fromString(id),
                publishProducerId = UUID.fromString(publishProducerId),
                publishedAt = publishedAt,
                nextRetry = nextRetry,
                completedAt = completedAt,
                attempt = attempt,
                properties = PropertySerializeUtils.deserialize(propertySerializerFactory, properties)
            )
        }

        companion object {
            fun of(propertySerializerFactory: PropertySerializerFactory, task: Task): TaskMongodb {
                return TaskMongodb(
                    task.name,
                    task.id.toString(),
                    task.publishProducerId.toString(),
                    task.publishedAt,
                    task.nextRetry,
                    task.completedAt,
                    task.attempt,
                    PropertySerializeUtils.serialize(propertySerializerFactory, task.properties)
                )
            }
        }
    }

    companion object {
        fun of(propertySerializerFactory: PropertySerializerFactory, queuedTask: QueuedTask): QueuedTaskMongodb {
            return QueuedTaskMongodb(
                queuedTask.task.id.toString(),
                TaskMongodb.of(propertySerializerFactory, queuedTask.task),
                queuedTask.attempt,
                queuedTask.queuedAt,
                queuedTask.priority,
                queuedTask.isActive,
                queuedTask.timeoutAt,
                queuedTask.assignedConsumer?.toString(),
                queuedTask.assignedAt
            )
        }
    }
}