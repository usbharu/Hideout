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

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import dev.usbharu.owl.broker.ModuleContext
import dev.usbharu.owl.broker.domain.model.consumer.ConsumerRepository
import dev.usbharu.owl.broker.domain.model.producer.ProducerRepository
import dev.usbharu.owl.broker.domain.model.queuedtask.QueuedTaskRepository
import dev.usbharu.owl.broker.domain.model.task.TaskRepository
import dev.usbharu.owl.broker.domain.model.taskdefinition.TaskDefinitionRepository
import dev.usbharu.owl.broker.domain.model.taskresult.TaskResultRepository
import org.bson.UuidRepresentation
import org.koin.core.module.Module
import org.koin.dsl.module

class MongoModuleContext : ModuleContext {
    override fun module(): Module {
        return module {
            single {
                val clientSettings =
                    MongoClientSettings.builder()
                        .applyConnectionString(
                            ConnectionString(
                                System.getProperty(
                                    "owl.broker.mongo.url",
                                    "mongodb://localhost:27017"
                                )
                            )
                        )
                        .uuidRepresentation(UuidRepresentation.STANDARD).build()

                MongoClient.create(clientSettings)
                    .getDatabase(System.getProperty("owl.broker.mongo.database", "mongo-test"))
            }
            single<ConsumerRepository> { MongodbConsumerRepository(get()) }
            single<ProducerRepository> { MongodbProducerRepository(get()) }
            single<QueuedTaskRepository> { MongodbQueuedTaskRepository(get(), get()) }
            single<TaskDefinitionRepository> { MongodbTaskDefinitionRepository(get()) }
            single<TaskRepository> { MongodbTaskRepository(get(), get()) }
            single<TaskResultRepository> { MongodbTaskResultRepository(get(), get()) }
        }
    }
}
