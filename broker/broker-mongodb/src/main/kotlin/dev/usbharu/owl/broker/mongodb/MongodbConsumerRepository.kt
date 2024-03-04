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
import dev.usbharu.owl.broker.domain.model.consumer.Consumer
import dev.usbharu.owl.broker.domain.model.consumer.ConsumerRepository
import kotlinx.coroutines.flow.singleOrNull
import org.bson.BsonType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonRepresentation
import org.koin.core.annotation.Singleton
import java.util.*

@Singleton
class MongodbConsumerRepository(database: MongoDatabase) : ConsumerRepository {

    private val collection = database.getCollection<ConsumerMongodb>("consumers")
    override suspend fun save(consumer: Consumer): Consumer {
        collection.replaceOne(Filters.eq("_id", consumer.id.toString()), ConsumerMongodb.of(consumer), ReplaceOptions().upsert(true))
        return consumer
    }

    override suspend fun findById(id: UUID): Consumer? {
        return collection.find(Filters.eq("_id", id.toString())).singleOrNull()?.toConsumer()
    }
}

data class ConsumerMongodb(
    @BsonId
    @BsonRepresentation(BsonType.STRING)
    val id: String,
    val name: String,
    val hostname: String,
    val tasks: List<String>
){

    fun toConsumer():Consumer{
        return Consumer(
            UUID.fromString(id), name, hostname, tasks
        )
    }
    companion object{
        fun of(consumer: Consumer):ConsumerMongodb{
            return ConsumerMongodb(
                consumer.id.toString(),
                consumer.name,
                consumer.hostname,
                consumer.tasks
            )
        }
    }
}