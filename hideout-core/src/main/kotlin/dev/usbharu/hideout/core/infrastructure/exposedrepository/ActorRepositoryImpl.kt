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

import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ActorRepositoryImpl(
    private val idGenerateService: IdGenerateService,
    private val actorResultRowMapper: ResultRowMapper<Actor>,
    private val actorQueryMapper: QueryMapper<Actor>
) : ActorRepository, AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun save(actor: Actor): Actor = query {
        val singleOrNull = Actors.selectAll().where { Actors.id eq actor.id }.forUpdate().empty()
        if (singleOrNull) {
            Actors.insert {
                it[id] = actor.id
                it[name] = actor.name
                it[domain] = actor.domain
                it[screenName] = actor.screenName
                it[description] = actor.description
                it[inbox] = actor.inbox
                it[outbox] = actor.outbox
                it[url] = actor.url
                it[createdAt] = actor.createdAt.toEpochMilli()
                it[publicKey] = actor.publicKey
                it[privateKey] = actor.privateKey
                it[keyId] = actor.keyId
                it[following] = actor.following
                it[followers] = actor.followers
                it[instance] = actor.instance
                it[locked] = actor.locked
                it[followersCount] = actor.followersCount
                it[followingCount] = actor.followingCount
                it[postsCount] = actor.postsCount
                it[lastPostAt] = actor.lastPostDate
                it[emojis] = actor.emojis.joinToString(",")
            }
        } else {
            Actors.update({ Actors.id eq actor.id }) {
                it[name] = actor.name
                it[domain] = actor.domain
                it[screenName] = actor.screenName
                it[description] = actor.description
                it[inbox] = actor.inbox
                it[outbox] = actor.outbox
                it[url] = actor.url
                it[createdAt] = actor.createdAt.toEpochMilli()
                it[publicKey] = actor.publicKey
                it[privateKey] = actor.privateKey
                it[keyId] = actor.keyId
                it[following] = actor.following
                it[followers] = actor.followers
                it[instance] = actor.instance
                it[locked] = actor.locked
                it[followersCount] = actor.followersCount
                it[followingCount] = actor.followingCount
                it[postsCount] = actor.postsCount
                it[lastPostAt] = actor.lastPostDate
                it[emojis] = actor.emojis.joinToString(",")
            }
        }
        return@query actor
    }

    override suspend fun findById(id: Long): Actor? = query {
        return@query Actors.selectAll().where { Actors.id eq id }.singleOrNull()?.let(actorResultRowMapper::map)
    }

    override suspend fun findByIdWithLock(id: Long): Actor? = query {
        return@query Actors.selectAll().where { Actors.id eq id }.forUpdate().singleOrNull()
            ?.let(actorResultRowMapper::map)
    }

    override suspend fun findAll(limit: Int, offset: Long): List<Actor> = query {
        return@query Actors.selectAll().limit(limit, offset).let(actorQueryMapper::map)
    }

    override suspend fun findByName(name: String): List<Actor> = query {
        return@query Actors.selectAll().where { Actors.name eq name }.let(actorQueryMapper::map)
    }

    override suspend fun findByNameAndDomain(name: String, domain: String): Actor? = query {
        return@query Actors.selectAll().where { Actors.name eq name and (Actors.domain eq domain) }.singleOrNull()
            ?.let(actorResultRowMapper::map)
    }

    override suspend fun findByNameAndDomainWithLock(name: String, domain: String): Actor? = query {
        return@query Actors.selectAll().where { Actors.name eq name and (Actors.domain eq domain) }.forUpdate()
            .singleOrNull()
            ?.let(actorResultRowMapper::map)
    }

    override suspend fun findByUrl(url: String): Actor? = query {
        return@query Actors.selectAll().where { Actors.url eq url }.singleOrNull()?.let(actorResultRowMapper::map)
    }

    override suspend fun findByUrlWithLock(url: String): Actor? = query {
        return@query Actors.selectAll().where { Actors.url eq url }.forUpdate().singleOrNull()
            ?.let(actorResultRowMapper::map)
    }

    override suspend fun findByIds(ids: List<Long>): List<Actor> = query {
        return@query Actors.selectAll().where { Actors.id inList ids }.let(actorQueryMapper::map)
    }

    override suspend fun findByKeyId(keyId: String): Actor? = query {
        return@query Actors.selectAll().where { Actors.keyId eq keyId }.singleOrNull()?.let(actorResultRowMapper::map)
    }

    override suspend fun delete(id: Long): Unit = query {
        Actors.deleteWhere { Actors.id.eq(id) }
    }

    override suspend fun nextId(): Long = idGenerateService.generateId()

    companion object {
        private val logger = LoggerFactory.getLogger(ActorRepositoryImpl::class.java)
    }
}

object Actors : Table("actors") {
    val id: Column<Long> = long("id")
    val name: Column<String> = varchar("name", length = 300)
    val domain: Column<String> = varchar("domain", length = 1000)
    val screenName: Column<String> = varchar("screen_name", length = 300)
    val description: Column<String> = varchar(
        "description",
        length = 10000
    )
    val inbox: Column<String> = varchar("inbox", length = 1000).uniqueIndex()
    val outbox: Column<String> = varchar("outbox", length = 1000).uniqueIndex()
    val url: Column<String> = varchar("url", length = 1000).uniqueIndex()
    val publicKey: Column<String> = varchar("public_key", length = 10000)
    val privateKey: Column<String?> = varchar(
        "private_key",
        length = 10000
    ).nullable()
    val createdAt: Column<Long> = long("created_at")
    val keyId = varchar("key_id", length = 1000)
    val following = varchar("following", length = 1000).nullable()
    val followers = varchar("followers", length = 1000).nullable()
    val instance = long("instance").references(Instance.id)
    val locked = bool("locked")
    val followingCount = integer("following_count")
    val followersCount = integer("followers_count")
    val postsCount = integer("posts_count")
    val lastPostAt = timestamp("last_post_at").nullable()
    val emojis = varchar("emojis", 3000)
    override val primaryKey: PrimaryKey = PrimaryKey(id)

    init {
        uniqueIndex(name, domain)
    }
}
