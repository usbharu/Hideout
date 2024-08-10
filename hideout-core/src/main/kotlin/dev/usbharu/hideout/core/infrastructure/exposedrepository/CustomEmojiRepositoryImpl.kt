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

import dev.usbharu.hideout.core.domain.model.emoji.CustomEmoji
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiRepository
import dev.usbharu.hideout.core.domain.model.emoji.EmojiId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.support.domain.Domain
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.net.URI

@Repository
class CustomEmojiRepositoryImpl : CustomEmojiRepository,
    AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun save(customEmoji: CustomEmoji): CustomEmoji = query {
        val singleOrNull =
            CustomEmojis.selectAll().where { CustomEmojis.id eq customEmoji.id.emojiId }.forUpdate().singleOrNull()
        if (singleOrNull == null) {
            CustomEmojis.insert {
                it[id] = customEmoji.id.emojiId
                it[name] = customEmoji.name
                it[domain] = customEmoji.domain.domain
                it[instanceId] = customEmoji.instanceId.instanceId
                it[url] = customEmoji.url.toString()
                it[category] = customEmoji.category
                it[createdAt] = customEmoji.createdAt
            }
        } else {
            CustomEmojis.update({ CustomEmojis.id eq customEmoji.id.emojiId }) {
                it[name] = customEmoji.name
                it[domain] = customEmoji.domain.domain
                it[instanceId] = customEmoji.instanceId.instanceId
                it[url] = customEmoji.url.toString()
                it[category] = customEmoji.category
                it[createdAt] = customEmoji.createdAt
            }
        }
        return@query customEmoji
    }

    override suspend fun findById(id: Long): CustomEmoji? = query {
        return@query CustomEmojis.selectAll().where { CustomEmojis.id eq id }.singleOrNull()?.toCustomEmoji()
    }

    override suspend fun delete(customEmoji: CustomEmoji): Unit = query {
        CustomEmojis.deleteWhere { id eq customEmoji.id.emojiId }
    }

    override suspend fun findByNamesAndDomain(names: List<String>, domain: String): List<CustomEmoji> = query {
        return@query CustomEmojis
            .selectAll()
            .where {
                CustomEmojis.name inList names and (CustomEmojis.domain eq domain)
            }
            .map { it.toCustomEmoji() }
    }

    override suspend fun findByIds(ids: List<Long>): List<CustomEmoji> = query {
        return@query CustomEmojis
            .selectAll()
            .where {
                CustomEmojis.id inList ids
            }
            .map { it.toCustomEmoji() }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CustomEmojiRepositoryImpl::class.java)
    }
}

fun ResultRow.toCustomEmoji(): CustomEmoji = CustomEmoji(
    id = EmojiId(this[CustomEmojis.id]),
    name = this[CustomEmojis.name],
    domain = Domain(this[CustomEmojis.domain]),
    instanceId = InstanceId(this[CustomEmojis.instanceId]),
    url = URI.create(this[CustomEmojis.url]),
    category = this[CustomEmojis.category],
    createdAt = this[CustomEmojis.createdAt]
)

fun ResultRow.toCustomEmojiOrNull(): CustomEmoji? {
    return CustomEmoji(
        id = EmojiId(this.getOrNull(CustomEmojis.id) ?: return null),
        name = this.getOrNull(CustomEmojis.name) ?: return null,
        domain = Domain(this.getOrNull(CustomEmojis.domain) ?: return null),
        instanceId = InstanceId(this.getOrNull(CustomEmojis.instanceId) ?: return null),
        url = URI.create(this.getOrNull(CustomEmojis.url) ?: return null),
        category = this[CustomEmojis.category],
        createdAt = this.getOrNull(CustomEmojis.createdAt) ?: return null
    )
}

object CustomEmojis : Table("emojis") {
    val id = long("id")
    val name = varchar("name", 1000)
    val domain = varchar("domain", 1000)
    val instanceId = long("instance_id").references(Instance.id)
    val url = varchar("url", 255).uniqueIndex()
    val category = varchar("category", 255).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey: PrimaryKey = PrimaryKey(id)

    init {
        uniqueIndex(name, instanceId)
    }
}
