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

import dev.usbharu.owl.broker.EmptyModuleContext
import dev.usbharu.owl.common.retry.DefaultRetryPolicyFactory
import dev.usbharu.owl.common.retry.ExponentialRetryPolicy
import dev.usbharu.owl.producer.api.OwlProducerBuilder

class EmbeddedOwlProducerBuilder : OwlProducerBuilder<EmbeddedOwlProducer, EmbeddedOwlProducerConfig> {
    var config: EmbeddedOwlProducerConfig = config()

    override fun config(): EmbeddedOwlProducerConfig {
        val embeddedOwlProducerConfig = EmbeddedOwlProducerConfig()

        with(embeddedOwlProducerConfig) {
            moduleContext = EmptyModuleContext
            retryPolicyFactory = DefaultRetryPolicyFactory(mapOf("" to ExponentialRetryPolicy()))
            name = "embedded-owl-producer"
            port = "50051"
        }

        return embeddedOwlProducerConfig
    }

    override fun build(): EmbeddedOwlProducer {
        return EmbeddedOwlProducer(
            config
        )
    }

    override fun apply(owlProducerConfig: EmbeddedOwlProducerConfig) {
        this.config = owlProducerConfig
    }

}

val EMBEDDED by lazy { EmbeddedOwlProducerBuilder() }