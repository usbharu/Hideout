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

package dev.usbharu.owl.broker

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import dev.usbharu.owl.broker.service.DefaultRetryPolicyFactory
import dev.usbharu.owl.broker.service.RetryPolicyFactory
import dev.usbharu.owl.common.property.PropertySerializerFactory
import dev.usbharu.owl.common.property.PropertySerializerFactoryImpl
import kotlinx.coroutines.runBlocking
import org.bson.UuidRepresentation
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule

fun main() {

    val moduleContext =
        Class.forName("dev.usbharu.owl.broker.mongodb.MongoModuleContext").newInstance() as ModuleContext


//    println(File(Thread.currentThread().contextClassLoader.getResource("dev/usbharu/owl/broker/mongodb").file).listFiles().joinToString())

    val koin = startKoin {
        printLogger()

        val module = module {
            single {
                val clientSettings =
                    MongoClientSettings.builder()
                        .applyConnectionString(ConnectionString("mongodb://agent1.build:27017"))
                        .uuidRepresentation(UuidRepresentation.STANDARD).build()


                MongoClient.create(clientSettings).getDatabase("mongo-test")
            }
            single<PropertySerializerFactory> {
                PropertySerializerFactoryImpl()
            }
            single<RetryPolicyFactory> {
                DefaultRetryPolicyFactory(emptyMap())
            }
        }
        modules(module, defaultModule, moduleContext.module())
    }

    val application = koin.koin.get<OwlBrokerApplication>()

    runBlocking {
        application.start(50051).join()
    }
}