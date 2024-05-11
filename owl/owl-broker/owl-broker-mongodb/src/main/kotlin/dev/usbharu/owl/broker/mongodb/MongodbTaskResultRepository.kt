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
import dev.usbharu.owl.broker.domain.model.taskresult.TaskResult
import dev.usbharu.owl.broker.domain.model.taskresult.TaskResultRepository
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
import java.util.*

@Singleton
class MongodbTaskResultRepository(
    database: MongoDatabase,
    private val propertySerializerFactory: PropertySerializerFactory
) : TaskResultRepository {

    private val collection = database.getCollection<TaskResultMongodb>("task_results")
    override suspend fun save(taskResult: TaskResult): TaskResult = withContext(Dispatchers.IO) {
        collection.replaceOne(
            Filters.eq(taskResult.id.toString()), TaskResultMongodb.of(propertySerializerFactory, taskResult),
            ReplaceOptions().upsert(true)
        )
        return@withContext taskResult
    }

    override fun findByTaskId(id: UUID): Flow<TaskResult> {
        return collection.find(Filters.eq(id.toString())).map { it.toTaskResult(propertySerializerFactory) }.flowOn(Dispatchers.IO)
    }
}

data class TaskResultMongodb(
    @BsonId
    @BsonRepresentation(BsonType.STRING)
    val id: String,
    val taskId: String,
    val success: Boolean,
    val attempt: Int,
    val result: Map<String, String>,
    val message: String
) {

    fun toTaskResult(propertySerializerFactory: PropertySerializerFactory): TaskResult {
        return TaskResult(
            UUID.fromString(id),
            UUID.fromString(taskId),
            success,
            attempt,
            PropertySerializeUtils.deserialize(propertySerializerFactory, result),
            message
        )
    }

    companion object {
        fun of(propertySerializerFactory: PropertySerializerFactory, taskResult: TaskResult): TaskResultMongodb {
            return TaskResultMongodb(
                taskResult.id.toString(),
                taskResult.taskId.toString(),
                taskResult.success,
                taskResult.attempt,
                PropertySerializeUtils.serialize(propertySerializerFactory, taskResult.result),
                taskResult.message
            )

        }

    }
}