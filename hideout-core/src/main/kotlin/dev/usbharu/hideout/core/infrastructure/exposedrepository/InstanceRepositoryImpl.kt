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

import dev.usbharu.hideout.core.domain.model.instance.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.net.URI
import dev.usbharu.hideout.core.domain.model.instance.Instance as InstanceEntity

@Repository
class InstanceRepositoryImpl : InstanceRepository,
    AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun save(instance: InstanceEntity): InstanceEntity = query {
        if (Instance.selectAll().where { Instance.id.eq(instance.id.instanceId) }.forUpdate().empty()) {
            Instance.insert {
                it[id] = instance.id.instanceId
                it[name] = instance.name.name
                it[description] = instance.description.description
                it[url] = instance.url.toString()
                it[iconUrl] = instance.iconUrl.toString()
                it[sharedInbox] = instance.sharedInbox?.toString()
                it[software] = instance.software.software
                it[version] = instance.version.version
                it[isBlocked] = instance.isBlocked
                it[isMuted] = instance.isMuted
                it[moderationNote] = instance.moderationNote.note
                it[createdAt] = instance.createdAt
            }
        } else {
            Instance.update({ Instance.id eq instance.id.instanceId }) {
                it[name] = instance.name.name
                it[description] = instance.description.description
                it[url] = instance.url.toString()
                it[iconUrl] = instance.iconUrl.toString()
                it[sharedInbox] = instance.sharedInbox?.toString()
                it[software] = instance.software.software
                it[version] = instance.version.version
                it[isBlocked] = instance.isBlocked
                it[isMuted] = instance.isMuted
                it[moderationNote] = instance.moderationNote.note
                it[createdAt] = instance.createdAt
            }
        }
        return@query instance
    }

    override suspend fun findById(id: InstanceId): InstanceEntity? = query {
        return@query Instance.selectAll().where { Instance.id eq id.instanceId }
            .singleOrNull()?.toInstance()
    }

    override suspend fun delete(instance: InstanceEntity): Unit = query {
        Instance.deleteWhere { id eq instance.id.instanceId }
    }

    override suspend fun findByUrl(url: URI): dev.usbharu.hideout.core.domain.model.instance.Instance? = query {
        return@query Instance.selectAll().where { Instance.url eq url.toString() }.singleOrNull()?.toInstance()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(InstanceRepositoryImpl::class.java)
    }
}

fun ResultRow.toInstance(): InstanceEntity {
    return InstanceEntity(
        id = InstanceId(this[Instance.id]),
        name = InstanceName(this[Instance.name]),
        description = InstanceDescription(this[Instance.description]),
        url = URI.create(this[Instance.url]),
        iconUrl = URI.create(this[Instance.iconUrl]),
        sharedInbox = this[Instance.sharedInbox]?.let { URI.create(it) },
        software = InstanceSoftware(this[Instance.software]),
        version = InstanceVersion(this[Instance.version]),
        isBlocked = this[Instance.isBlocked],
        isMuted = this[Instance.isMuted],
        moderationNote = InstanceModerationNote(this[Instance.moderationNote]),
        createdAt = this[Instance.createdAt]
    )
}

object Instance : Table("instance") {
    val id = long("id")
    val name = varchar("name", 1000)
    val description = varchar("description", 5000)
    val url = varchar("url", 255).uniqueIndex()
    val iconUrl = varchar("icon_url", 255)
    val sharedInbox = varchar("shared_inbox", 255).nullable().uniqueIndex()
    val software = varchar("software", 255)
    val version = varchar("version", 255)
    val isBlocked = bool("is_blocked")
    val isMuted = bool("is_muted")
    val moderationNote = varchar("moderation_note", 10000)
    val createdAt = timestamp("created_at")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
