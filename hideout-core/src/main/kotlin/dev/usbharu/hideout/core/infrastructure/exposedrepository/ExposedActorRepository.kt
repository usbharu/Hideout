package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.actor.*
import dev.usbharu.hideout.core.domain.model.shared.Domain
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.domain.shared.repository.DomainEventPublishableRepository
import dev.usbharu.hideout.core.infrastructure.exposed.QueryMapper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedActorRepository(
    private val actorQueryMapper: QueryMapper<Actor>,
    override val domainEventPublisher: DomainEventPublisher,
) : AbstractRepository(),
    DomainEventPublishableRepository<Actor>,
    ActorRepository {
    override val logger: Logger
        get() = Companion.logger

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedActorRepository::class.java)
    }

    override suspend fun save(actor: Actor): Actor {
        query {
            Actors.upsert {
                it[id] = actor.id.id
                it[name] = actor.name.name
                it[domain] = actor.domain.domain
                it[screenName] = actor.screenName.screenName
                it[description] = actor.description.description
                it[inbox] = actor.inbox.toString()
                it[outbox] = actor.outbox.toString()
                it[url] = actor.outbox.toString()
                it[publicKey] = actor.publicKey.publicKey
                it[privateKey] = actor.privateKey?.privateKey
                it[createdAt] = actor.createdAt
                it[keyId] = actor.keyId.keyId
                it[following] = actor.followingEndpoint?.toString()
                it[followers] = actor.followersEndpoint?.toString()
                it[instance] = actor.instance.instanceId
                it[locked] = actor.locked
                it[followingCount] = actor.followingCount?.relationshipCount
                it[followersCount] = actor.followersCount?.relationshipCount
                it[postsCount] = actor.postsCount.postsCount
                it[lastPostAt] = actor.lastPostAt
                it[lastUpdateAt] = actor.lastUpdateAt
                it[suspend] = actor.suspend
                it[moveTo] = actor.moveTo?.id
                it[emojis] = actor.emojis.joinToString(",")
            }
            ActorsAlsoKnownAs.deleteWhere {
                actorId eq actor.id.id
            }
            ActorsAlsoKnownAs.batchInsert(actor.alsoKnownAs) {
                this[ActorsAlsoKnownAs.actorId] = actor.id.id
                this[ActorsAlsoKnownAs.alsoKnownAs] = it.id
            }
        }
        update(actor)
        return actor
    }

    override suspend fun delete(actor: Actor) {
        query {
            Actors.deleteWhere { id eq actor.id.id }
            ActorsAlsoKnownAs.deleteWhere { actorId eq actor.id.id }
        }
        update(actor)
    }

    override suspend fun findById(id: ActorId): Actor? {
        return query {
            Actors
                .leftJoin(ActorsAlsoKnownAs, onColumn = { Actors.id }, otherColumn = { actorId })
                .selectAll()
                .where {
                    Actors.id eq id.id
                }
                .let(actorQueryMapper::map)
                .firstOrNull()
        }
    }

    override suspend fun findByNameAndDomain(name: String, domain: String): Actor? {
        return query {
            Actors
                .leftJoin(ActorsAlsoKnownAs, onColumn = { id }, otherColumn = { actorId })
                .selectAll()
                .where {
                    Actors.name eq name and (Actors.domain eq domain)
                }
                .let(actorQueryMapper::map)
                .firstOrNull()
        }
    }
}

object Actors : Table("actors") {
    val id = long("id")
    val name = varchar("name", ActorName.length)
    val domain = varchar("domain", Domain.length)
    val screenName = varchar("screen_name", ActorScreenName.length)
    val description = varchar("description", ActorDescription.length)
    val inbox = varchar("inbox", 1000).uniqueIndex()
    val outbox = varchar("outbox", 1000).uniqueIndex()
    val url = varchar("url", 1000).uniqueIndex()
    val publicKey = varchar("public_key", 10000)
    val privateKey = varchar("private_key", 100000).nullable()
    val createdAt = timestamp("created_at")
    val keyId = varchar("key_id", 1000)
    val following = varchar("following", 1000).nullable()
    val followers = varchar("followers", 1000).nullable()
    val instance = long("instance").references(Instance.id)
    val locked = bool("locked")
    val followingCount = integer("following_count").nullable()
    val followersCount = integer("followers_count").nullable()
    val postsCount = integer("posts_count")
    val lastPostAt = timestamp("last_post_at").nullable()
    val lastUpdateAt = timestamp("last_update_at")
    val suspend = bool("suspend")
    val moveTo = long("move_to").references(id).nullable()
    val emojis = varchar("emojis", 3000)
    val deleted = bool("deleted")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(name, domain)
    }
}

object ActorsAlsoKnownAs : Table("actor_alsoknownas") {
    val actorId =
        long("actor_id").references(Actors.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val alsoKnownAs = long("also_known_as").references(Actors.id, ReferenceOption.CASCADE, ReferenceOption.CASCADE)

    override val primaryKey: PrimaryKey = PrimaryKey(actorId, alsoKnownAs)
}
