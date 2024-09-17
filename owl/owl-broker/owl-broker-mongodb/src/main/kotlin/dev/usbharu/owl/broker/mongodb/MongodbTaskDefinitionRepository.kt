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
import dev.usbharu.owl.broker.domain.model.taskdefinition.TaskDefinition
import dev.usbharu.owl.broker.domain.model.taskdefinition.TaskDefinitionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.withContext
import org.bson.BsonType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonRepresentation

class MongodbTaskDefinitionRepository(database: MongoDatabase) : TaskDefinitionRepository {

    private val collection = database.getCollection<TaskDefinitionMongodb>("task_definition")
    override suspend fun save(taskDefinition: TaskDefinition): TaskDefinition = withContext(Dispatchers.IO) {
        collection.replaceOne(
            Filters.eq("_id", taskDefinition.name),
            TaskDefinitionMongodb.of(taskDefinition),
            ReplaceOptions().upsert(true)
        )
        return@withContext taskDefinition
    }

    override suspend fun deleteByName(name: String): Unit = withContext(Dispatchers.IO) {
        collection.deleteOne(Filters.eq("_id", name))
    }

    override suspend fun findByName(name: String): TaskDefinition? = withContext(Dispatchers.IO) {
        return@withContext collection.find(Filters.eq("_id", name)).singleOrNull()?.toTaskDefinition()
    }
}

data class TaskDefinitionMongodb(
    @BsonId
    @BsonRepresentation(BsonType.STRING)
    val name: String,
    val priority: Int,
    val maxRetry: Int,
    val timeoutMilli: Long,
    val propertyDefinitionHash: Long,
    val retryPolicy: String
) {
    fun toTaskDefinition(): TaskDefinition {
        return TaskDefinition(
            name = name,
            priority = priority,
            maxRetry = maxRetry,
            timeoutMilli = timeoutMilli,
            propertyDefinitionHash = propertyDefinitionHash,
            retryPolicy = retryPolicy
        )
    }

    companion object {
        fun of(taskDefinition: TaskDefinition): TaskDefinitionMongodb {
            return TaskDefinitionMongodb(
                name = taskDefinition.name,
                priority = taskDefinition.priority,
                maxRetry = taskDefinition.maxRetry,
                timeoutMilli = taskDefinition.timeoutMilli,
                propertyDefinitionHash = taskDefinition.propertyDefinitionHash,
                retryPolicy = taskDefinition.retryPolicy
            )
        }
    }
}
