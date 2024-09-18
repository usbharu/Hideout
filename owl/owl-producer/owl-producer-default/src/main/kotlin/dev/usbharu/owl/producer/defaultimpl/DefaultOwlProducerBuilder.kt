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

package dev.usbharu.owl.producer.defaultimpl

import dev.usbharu.owl.producer.api.OwlProducerBuilder
import io.grpc.ManagedChannelBuilder

class DefaultOwlProducerBuilder : OwlProducerBuilder<DefaultOwlProducer, DefaultOwlProducerConfig> {

    var config: DefaultOwlProducerConfig = config()

    override fun config(): DefaultOwlProducerConfig {
        val defaultOwlProducerConfig = DefaultOwlProducerConfig()

        with(defaultOwlProducerConfig) {
            channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build()
        }

        return defaultOwlProducerConfig
    }

    override fun build(): DefaultOwlProducer {
        return DefaultOwlProducer(
            config
        )
    }

    override fun apply(owlProducerConfig: DefaultOwlProducerConfig) {
        this.config = owlProducerConfig
    }
}

val DEFAULT by lazy { DefaultOwlProducerBuilder() }
