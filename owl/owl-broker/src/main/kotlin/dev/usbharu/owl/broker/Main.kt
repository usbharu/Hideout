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

import dev.usbharu.owl.common.retry.DefaultRetryPolicyFactory
import dev.usbharu.owl.common.retry.ExponentialRetryPolicy
import dev.usbharu.owl.common.retry.RetryPolicyFactory
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import org.slf4j.LoggerFactory
import java.util.*

val logger = LoggerFactory.getLogger("MAIN")

fun main() {
    val moduleContexts = ServiceLoader.load(ModuleContext::class.java)

    val moduleContext = moduleContexts.first()

    logger.info("Use module name: {}", moduleContext)


    val koin = startKoin {
        printLogger()

        val module = module {
            single<RetryPolicyFactory> {
                DefaultRetryPolicyFactory(mapOf("" to ExponentialRetryPolicy()))
            }
        }
        modules(defaultModule, module, moduleContext.module())
    }

    val application = koin.koin.get<OwlBrokerApplication>()

    runBlocking {
        application.start(50051).join()
    }
}