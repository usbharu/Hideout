package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionId
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.domain.shared.repository.DomainEventPublishableRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedReactionRepository(override val domainEventPublisher: DomainEventPublisher) : ReactionRepository,
    AbstractRepository(), DomainEventPublishableRepository<Reaction> {

    override val logger: Logger
        get() = Companion.logger


    override suspend fun save(reaction: Reaction): Reaction {
        return query {
            Reactions.upsert {
                it[Reactions.id] = reaction.id.value
                it[Reactions.postId] = reaction.postId.id
                it[Reactions.actorId] = reaction.actorId.id
                it[Reactions.customEmojiId] = reaction.customEmojiId?.emojiId
                it[Reactions.unicodeEmoji] = reaction.unicodeEmoji.name
            }
            onComplete {
                update(reaction)
            }
            reaction
        }
    }

    override suspend fun findById(reactionId: ReactionId): Reaction? {
        return query {
            Reactions.selectAll().where {
                Reactions.id eq reactionId.value
            }.singleOrNull()?.toReaction()
        }
    }

    override suspend fun delete(reaction: Reaction) {
        return query {
            Reactions.deleteWhere {
                Reactions.id eq reaction.id.value
            }
            onComplete {
                update(reaction)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedReactionRepository::class.java)
    }
}

fun ResultRow.toReaction(): Reaction {
    return Reaction(
        ReactionId(this[Reactions.id]),
        PostId(this[Reactions.postId]),
        ActorId(this[Reactions.actorId]),
        this[Reactions.customEmojiId]?.let { CustomEmojiId(it) },
        UnicodeEmoji(this[Reactions.unicodeEmoji]),
        this[Reactions.createdAt]
    )

}

object Reactions : Table("reactions") {
    val id = long("id")
    val postId = long("post_id").references(Posts.id)
    val actorId = long("actor_id").references(Actors.id)
    val customEmojiId = long("custom_emoji_id").references(CustomEmojis.id).nullable()
    val unicodeEmoji = varchar("unicode_emoji", 100)
    val createdAt = timestamp("created_at")
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}