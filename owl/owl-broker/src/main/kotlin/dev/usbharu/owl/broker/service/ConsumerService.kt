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

import dev.usbharu.owl.broker.domain.model.consumer.Consumer
import dev.usbharu.owl.broker.domain.model.consumer.ConsumerRepository
import org.slf4j.LoggerFactory
import java.util.*

interface ConsumerService {
    suspend fun registerConsumer(registerConsumerRequest: RegisterConsumerRequest): UUID
}

class ConsumerServiceImpl(private val consumerRepository: ConsumerRepository) : ConsumerService {
    override suspend fun registerConsumer(registerConsumerRequest: RegisterConsumerRequest): UUID {
        val id = UUID.randomUUID()

        consumerRepository.save(
            Consumer(
                id,
                registerConsumerRequest.name,
                registerConsumerRequest.hostname,
                registerConsumerRequest.tasks
            )
        )

        logger.info(
            "Register a new Consumer. name: {} hostname: {} tasks: {}",
            registerConsumerRequest.name,
            registerConsumerRequest.hostname,
            registerConsumerRequest.tasks.size
        )

        return id
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConsumerServiceImpl::class.java)
    }
}

data class RegisterConsumerRequest(
    val name: String,
    val hostname: String,
    val tasks: List<String>
)
