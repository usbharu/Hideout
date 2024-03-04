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

package dev.usbharu.producer.impl.datasource

import dev.usbharu.producer.impl.OwlTaskDatasource
import java.util.ServiceLoader
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

class ServiceProviderDatasourceFactory : DatasourceFactory {
    override suspend fun create(): OwlTaskDatasource {
        val serviceLoader: ServiceLoader<OwlTaskDatasource> = ServiceLoader.load(OwlTaskDatasource::class.java)

        return serviceLoader.findFirst().getOrElse {
            throw IllegalStateException("")
        }
    }
}