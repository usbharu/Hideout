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

package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.meta.Jwt
import dev.usbharu.hideout.core.domain.model.meta.MetaRepository
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class MetaRepositoryImpl : MetaRepository {

    override suspend fun save(meta: dev.usbharu.hideout.core.domain.model.meta.Meta) {
        if (Meta.selectAll().where { Meta.id eq 1 }.empty()) {
            Meta.insert {
                it[id] = 1
                it[version] = meta.version
                it[kid] = UUID.randomUUID().toString()
                it[jwtPrivateKey] = meta.jwt.privateKey
                it[jwtPublicKey] = meta.jwt.publicKey
            }
        } else {
            Meta.update({ Meta.id eq 1 }) {
                it[version] = meta.version
                it[kid] = UUID.randomUUID().toString()
                it[jwtPrivateKey] = meta.jwt.privateKey
                it[jwtPublicKey] = meta.jwt.publicKey
            }
        }
    }

    override suspend fun get(): dev.usbharu.hideout.core.domain.model.meta.Meta? {
        return Meta.selectAll().where { Meta.id eq 1 }.singleOrNull()?.let {
            dev.usbharu.hideout.core.domain.model.meta.Meta(
                it[Meta.version],
                Jwt(UUID.fromString(it[Meta.kid]), it[Meta.jwtPrivateKey], it[Meta.jwtPublicKey])
            )
        }
    }
}

object Meta : Table("meta_info") {
    val id: Column<Long> = long("id")
    val version: Column<String> = varchar("version", 1000)
    val kid: Column<String> = varchar("kid", 1000)
    val jwtPrivateKey: Column<String> = varchar("jwt_private_key", 100000)
    val jwtPublicKey: Column<String> = varchar("jwt_public_key", 100000)
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
