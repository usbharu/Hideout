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
import kotlinx.coroutines.withContext
import org.bson.BsonType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonRepresentation
import org.koin.core.annotation.Singleton
import java.time.Instant
import java.util.*

@Singleton
class MongodbTaskRepository(database: MongoDatabase, private val propertySerializerFactory: PropertySerializerFactory) :
    TaskRepository {

    private val collection = database.getCollection<TaskMongodb>("tasks")
    override suspend fun save(task: Task): Task = withContext(Dispatchers.IO) {
        collection.replaceOne(
            Filters.eq("_id", task.id.toString()), TaskMongodb.of(propertySerializerFactory, task),
            ReplaceOptions().upsert(true)
        )
        return@withContext task
    }

    override fun findByNextRetryBefore(timestamp: Instant): Flow<Task> {
        return collection.find(Filters.lte(TaskMongodb::nextRetry.name, timestamp))
            .map { it.toTask(propertySerializerFactory) }.flowOn(Dispatchers.IO)
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