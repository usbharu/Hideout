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

package dev.usbharu.owl.producer.embedded

import dev.usbharu.owl.producer.api.OwlProducerBuilder

class EmbeddedGrpcOwlProducerBuilder : OwlProducerBuilder<EmbeddedGrpcOwlProducer, EmbeddedGrpcOwlProducerConfig> {
    private var config = config()

    override fun config(): EmbeddedGrpcOwlProducerConfig {
        return EmbeddedGrpcOwlProducerConfig()
    }

    override fun build(): EmbeddedGrpcOwlProducer {
        return EmbeddedGrpcOwlProducer(
            config
        )
    }

    override fun apply(owlProducerConfig: EmbeddedGrpcOwlProducerConfig) {
        this.config = owlProducerConfig
    }
}

val EMBEDDED_GRPC by lazy { EmbeddedGrpcOwlProducerBuilder() }
