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
import dev.usbharu.owl.broker.domain.model.producer.Producer
import dev.usbharu.owl.broker.domain.model.producer.ProducerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton
import java.time.Instant
import java.util.*

@Singleton
class MongodbProducerRepository(database: MongoDatabase) : ProducerRepository {

    private val collection = database.getCollection<ProducerMongodb>("producers")

    override suspend fun save(producer: Producer): Producer = withContext(Dispatchers.IO) {
        collection.replaceOne(
            Filters.eq("_id", producer.id.toString()),
            ProducerMongodb.of(producer),
            ReplaceOptions().upsert(true)
        )
        return@withContext producer
    }
}

data class ProducerMongodb(
    val id: String,
    val name: String,
    val hostname: String,
    val registeredTask: List<String>,
    val createdAt: Instant
) {
    fun toProducer(): Producer {
        return Producer(
            id = UUID.fromString(id),
            name = name,
            hostname = hostname,
            registeredTask = registeredTask,
            createdAt = createdAt
        )
    }

    companion object {
        fun of(producer: Producer): ProducerMongodb {
            return ProducerMongodb(
                id = producer.id.toString(),
                name = producer.name,
                hostname = producer.hostname,
                registeredTask = producer.registeredTask,
                createdAt = producer.createdAt
            )
        }
    }
}