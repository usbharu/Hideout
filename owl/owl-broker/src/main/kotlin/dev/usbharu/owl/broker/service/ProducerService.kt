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

package dev.usbharu.owl.broker.service

import dev.usbharu.owl.broker.domain.model.producer.Producer
import dev.usbharu.owl.broker.domain.model.producer.ProducerRepository
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

interface ProducerService {
    suspend fun registerProducer(producer: RegisterProducerRequest): UUID
}

class ProducerServiceImpl(private val producerRepository: ProducerRepository) : ProducerService {
    override suspend fun registerProducer(producer: RegisterProducerRequest): UUID {
        val id = UUID.randomUUID()

        val saveProducer = Producer(
            id = id,
            name = producer.name,
            hostname = producer.hostname,
            registeredTask = emptyList(),
            createdAt = Instant.now()
        )

        producerRepository.save(saveProducer)

        logger.info("Register a new Producer. name: {} hostname: {}", saveProducer.name, saveProducer.hostname)
        return id
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ProducerServiceImpl::class.java)
    }
}

data class RegisterProducerRequest(
    val name: String,
    val hostname: String
)
