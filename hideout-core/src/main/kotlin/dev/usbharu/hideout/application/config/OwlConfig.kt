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

package dev.usbharu.hideout.application.config

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.owl.broker.ModuleContext
import dev.usbharu.owl.common.property.*
import dev.usbharu.owl.common.retry.RetryPolicyFactory
import dev.usbharu.owl.producer.api.OWL
import dev.usbharu.owl.producer.api.OwlProducer
import dev.usbharu.owl.producer.defaultimpl.DEFAULT
import dev.usbharu.owl.producer.embedded.EMBEDDED
import dev.usbharu.owl.producer.embedded.EMBEDDED_GRPC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class OwlConfig(private val producerConfig: ProducerConfig) {
    @Bean
    fun producer(
        @Autowired(required = false) retryPolicyFactory: RetryPolicyFactory? = null,
        @Qualifier("activitypub") objectMapper: ObjectMapper,
    ): OwlProducer {
        return when (producerConfig.mode) {
            ProducerMode.EMBEDDED -> {
                OWL(EMBEDDED) {
                    if (retryPolicyFactory != null) {
                        this.retryPolicyFactory = retryPolicyFactory
                    }
                    if (producerConfig.port != null) {
                        this.port = producerConfig.port.toString()
                    }
                    val moduleContext = ServiceLoader.load(ModuleContext::class.java).firstOrNull()
                    if (moduleContext != null) {
                        this.moduleContext = moduleContext
                    }
                    this.propertySerializerFactory = CustomPropertySerializerFactory(
                        setOf(
                            IntegerPropertySerializer(),
                            StringPropertyValueSerializer(),
                            DoublePropertySerializer(),
                            BooleanPropertySerializer(),
                            LongPropertySerializer(),
                            FloatPropertySerializer(),
                            ObjectPropertySerializer(objectMapper),
                        )
                    )
                }
            }

            ProducerMode.GRPC -> {
                OWL(EMBEDDED_GRPC) {
                }
            }

            ProducerMode.EMBEDDED_GRPC -> {
                OWL(DEFAULT) {
                }
            }
        }
    }
}

@ConfigurationProperties("hideout.owl.producer")
data class ProducerConfig(val mode: ProducerMode = ProducerMode.EMBEDDED, val port: Int? = null)

enum class ProducerMode {
    GRPC,
    EMBEDDED,
    EMBEDDED_GRPC
}
