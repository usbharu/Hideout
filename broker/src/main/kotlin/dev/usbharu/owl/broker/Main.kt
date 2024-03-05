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

import dev.usbharu.owl.broker.service.DefaultRetryPolicyFactory
import dev.usbharu.owl.broker.service.RetryPolicyFactory
import dev.usbharu.owl.common.property.PropertySerializerFactory
import dev.usbharu.owl.common.property.PropertySerializerFactoryImpl
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import java.util.*

fun main() {
    val moduleContext = ServiceLoader.load(ModuleContext::class.java).first()

    val koin = startKoin {
        printLogger()

        val module = module {
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