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

package dev.usbharu.hideout.application.service.init

import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.exception.NotInitException
import org.springframework.stereotype.Service

@Service
class MetaServiceImpl(private val metaRepository: MetaRepository, private val transaction: Transaction) :
    MetaService {
    override suspend fun getMeta(): Meta =
        transaction.transaction { metaRepository.get() ?: throw NotInitException("Meta is null") }

    override suspend fun updateMeta(meta: Meta): Unit = transaction.transaction {
        metaRepository.save(meta)
    }

    override suspend fun getJwtMeta(): Jwt = getMeta().jwt
}
