package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.springframework.stereotype.Repository

@Repository
class ActorRepositoryImpl(
    private val idGenerateService: IdGenerateService,
    private val actorResultRowMapper: ResultRowMapper<Actor>
) :
    ActorRepository {

    override suspend fun save(actor: Actor): Actor {
        val singleOrNull = Actors.select { Actors.id eq actor.id }.empty()
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
            }
        }
        return actor
    }

    override suspend fun findById(id: Long): Actor? =
        Actors.select { Actors.id eq id }.singleOrNull()?.let(actorResultRowMapper::map)

    override suspend fun delete(id: Long) {
        Actors.deleteWhere { Actors.id.eq(id) }
    }

    override suspend fun nextId(): Long = idGenerateService.generateId()
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
    val instance = long("instance").references(Instance.id).nullable()
    val locked = bool("locked")
    val followingCount = integer("following_count")
    val followersCount = integer("followers_count")
    val postsCount = integer("posts_count")
    val lastPostAt = timestamp("last_post_at").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)

    init {
        uniqueIndex(name, domain)
    }
}
