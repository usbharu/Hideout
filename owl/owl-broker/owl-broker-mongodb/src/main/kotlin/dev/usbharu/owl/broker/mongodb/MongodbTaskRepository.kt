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

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOneModel
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import dev.usbharu.owl.broker.domain.model.task.Task
import dev.usbharu.owl.broker.domain.model.task.TaskRepository
import dev.usbharu.owl.common.property.PropertySerializeUtils
import dev.usbharu.owl.common.property.PropertySerializerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.withContext
import org.bson.BsonType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonRepresentation
import java.time.Instant
import java.util.*

class MongodbTaskRepository(database: MongoDatabase, private val propertySerializerFactory: PropertySerializerFactory) :
    TaskRepository {

    private val collection = database.getCollection<TaskMongodb>("tasks")
    override suspend fun save(task: Task): Task = withContext(Dispatchers.IO) {
        collection.replaceOne(
            Filters.eq("_id", task.id.toString()),
            TaskMongodb.of(propertySerializerFactory, task),
            ReplaceOptions().upsert(true)
        )
        return@withContext task
    }

    override suspend fun saveAll(tasks: List<Task>): Unit = withContext(Dispatchers.IO) {
        collection.bulkWrite(
            tasks.map {
                ReplaceOneModel(
                    Filters.eq(it.id.toString()),
                    TaskMongodb.of(propertySerializerFactory, it),
                    ReplaceOptions().upsert(true)
                )
            }
        )
    }

    override fun findByNextRetryBeforeAndCompletedAtIsNull(timestamp: Instant): Flow<Task> {
        return collection.find(
            Filters.and(
                Filters.lte(TaskMongodb::nextRetry.name, timestamp),
                Filters.eq(TaskMongodb::completedAt.name, null)
            )
        )
            .map { it.toTask(propertySerializerFactory) }.flowOn(Dispatchers.IO)
    }

    override suspend fun findById(uuid: UUID): Task? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq(uuid.toString())).singleOrNull()?.toTask(propertySerializerFactory)
    }

    override suspend fun findByIdAndUpdate(id: UUID, task: Task) {
        collection.replaceOne(
            Filters.eq("_id", task.id.toString()),
            TaskMongodb.of(propertySerializerFactory, task),
            ReplaceOptions().upsert(false)
        )
    }

    override fun findByPublishProducerIdAndCompletedAtIsNotNull(publishProducerId: UUID): Flow<Task> {
        return collection
            .find(Filters.eq(TaskMongodb::publishProducerId.name, publishProducerId.toString()))
            .map { it.toTask(propertySerializerFactory) }
    }
}

data class TaskMongodb(
    val name: String,
    @BsonId
    @BsonRepresentation(BsonType.STRING)
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
                name = task.name,
                id = task.id.toString(),
                publishProducerId = task.publishProducerId.toString(),
                publishedAt = task.publishedAt,
                nextRetry = task.nextRetry,
                completedAt = task.completedAt,
                attempt = task.attempt,
                properties = PropertySerializeUtils.serialize(propertySerializerFactory, task.properties)
            )
        }
    }
}
