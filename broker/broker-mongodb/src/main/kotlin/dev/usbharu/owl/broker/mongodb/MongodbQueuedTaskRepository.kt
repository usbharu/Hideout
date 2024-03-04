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
import com.mongodb.client.model.Updates.set
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import dev.usbharu.owl.broker.domain.model.queuedtask.QueuedTask
import dev.usbharu.owl.broker.domain.model.queuedtask.QueuedTaskRepository
import dev.usbharu.owl.common.property.PropertySerializerFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.bson.BsonType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonRepresentation
import org.koin.core.annotation.Singleton
import java.time.Instant
import java.util.*

@Singleton
class MongodbQueuedTaskRepository(private val propertySerializerFactory: PropertySerializerFactory,database: MongoDatabase) : QueuedTaskRepository {

    private val collection = database.getCollection<QueuedTaskMongodb>("queued_task")
    override suspend fun save(queuedTask: QueuedTask): QueuedTask {
        collection.replaceOne(
            eq("_id", queuedTask.task.id.toString()), QueuedTaskMongodb.of(propertySerializerFactory,queuedTask),
            ReplaceOptions().upsert(true)
        )
        return queuedTask
    }

    override suspend fun findByTaskIdAndAssignedConsumerIsNullAndUpdate(id: UUID, update: QueuedTask): QueuedTask {
        val findOneAndUpdate = collection.findOneAndUpdate(
            and(
                eq("_id", id.toString()),
                eq(QueuedTaskMongodb::assignedConsumer.name, null)
            ),
            listOf(
                set(QueuedTaskMongodb::assignedConsumer.name, update.assignedConsumer),
                set(QueuedTaskMongodb::assignedAt.name, update.assignedAt)
            ),
            FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER)
        )
        if (findOneAndUpdate == null) {
            TODO()
        }
        return findOneAndUpdate.toQueuedTask(propertySerializerFactory)
    }

    override fun findByTaskNameInAndAssignedConsumerIsNullAndOrderByPriority(
        tasks: List<String>,
        limit: Int
    ): Flow<QueuedTask> {
        return collection.find<QueuedTaskMongodb>(
            and(
                `in`("task.name", tasks),
                eq(QueuedTaskMongodb::assignedConsumer.name, null)
            )
        ).map { it.toQueuedTask(propertySerializerFactory) }
    }
}

data class QueuedTaskMongodb(
    @BsonId
    @BsonRepresentation(BsonType.STRING)
    val id: String,
    val task: TaskMongodb,
    val attempt: Int,
    val queuedAt: Instant,
    val assignedConsumer: String?,
    val assignedAt: Instant?
) {

    fun toQueuedTask(propertySerializerFactory: PropertySerializerFactory): QueuedTask {
        return QueuedTask(
            attempt,
            queuedAt,
            task.toTask(propertySerializerFactory),
            UUID.fromString(assignedConsumer),
            assignedAt
        )
    }

    companion object {
        fun of(propertySerializerFactory: PropertySerializerFactory,queuedTask: QueuedTask): QueuedTaskMongodb {
            return QueuedTaskMongodb(
                queuedTask.task.id.toString(),
                TaskMongodb.of(propertySerializerFactory,queuedTask.task),
                queuedTask.attempt,
                queuedTask.queuedAt,
                queuedTask.assignedConsumer?.toString(),
                queuedTask.assignedAt
            )
        }
    }
}