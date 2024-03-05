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
import org.bson.UuidRepresentation
import org.koin.ksp.generated.module

class MongoModuleContext : ModuleContext {
    override fun module(): org.koin.core.module.Module {
        val module = MongoModule().module
        module.includes(org.koin.dsl.module {
            single {
                val clientSettings =
                    MongoClientSettings.builder()
                        .applyConnectionString(ConnectionString("mongodb://agent1.build:27017"))
                        .uuidRepresentation(UuidRepresentation.STANDARD).build()


                MongoClient.create(clientSettings).getDatabase("mongo-test")
            }
        })
        return module
    }
}