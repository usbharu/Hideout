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

package dev.usbharu.hideout.core.domain.model.actor

import org.springframework.stereotype.Repository

@Repository
@Suppress("TooManyFunctions")
interface ActorRepository {
    suspend fun save(actor: Actor): Actor

    suspend fun findById(id: Long): Actor?

    suspend fun findByIdWithLock(id: Long): Actor?

    suspend fun findAll(limit: Int, offset: Long): List<Actor>

    suspend fun findByName(name: String): List<Actor>

    suspend fun findByNameAndDomain(name: String, domain: String): Actor?

    suspend fun findByNameAndDomainWithLock(name: String, domain: String): Actor?

    suspend fun findByUrl(url: String): Actor?

    suspend fun findByUrlWithLock(url: String): Actor?

    suspend fun findByIds(ids: List<Long>): List<Actor>

    suspend fun findByKeyId(keyId: String): Actor?

    suspend fun delete(id: Long)

    suspend fun nextId(): Long
}
