package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmoji
import dev.usbharu.hideout.core.domain.model.emoji.Emoji
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ReactionRepositoryImpl(
    private val idGenerateService: IdGenerateService
) : ReactionRepository, AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(reaction: Reaction): Reaction = query {
        if (Reactions.select { Reactions.id eq reaction.id }.forUpdate().empty()) {
            Reactions.insert {
                it[id] = reaction.id
                if (reaction.emoji is CustomEmoji) {
                    it[customEmojiId] = reaction.emoji.id
                    it[unicodeEmoji] = null
                } else {
                    it[customEmojiId] = null
                    it[unicodeEmoji] = reaction.emoji.name
                }
                it[postId] = reaction.postId
                it[actorId] = reaction.actorId
            }
        } else {
            Reactions.update({ Reactions.id eq reaction.id }) {
                if (reaction.emoji is CustomEmoji) {
                    it[customEmojiId] = reaction.emoji.id
                    it[unicodeEmoji] = null
                } else {
                    it[customEmojiId] = null
                    it[unicodeEmoji] = reaction.emoji.name
                }
                it[postId] = reaction.postId
                it[actorId] = reaction.actorId
            }
        }
        return@query reaction
    }

    override suspend fun delete(reaction: Reaction): Reaction = query {
        if (reaction.emoji is CustomEmoji) {
            Reactions.deleteWhere {
                id.eq(reaction.id).and(postId.eq(reaction.postId)).and(actorId.eq(reaction.actorId))
                    .and(customEmojiId.eq(reaction.emoji.id))
            }
        } else {
            Reactions.deleteWhere {
                id.eq(reaction.id).and(postId.eq(reaction.postId)).and(actorId.eq(reaction.actorId))
                    .and(unicodeEmoji.eq(reaction.emoji.name))
            }
        }
        return@query reaction
    }

    override suspend fun deleteByPostId(postId: Long): Int = query {
        return@query Reactions.deleteWhere {
            Reactions.postId eq postId
        }
    }

    override suspend fun deleteByActorId(actorId: Long): Int = query {
        return@query Reactions.deleteWhere {
            Reactions.actorId eq actorId
        }
    }

    override suspend fun deleteByPostIdAndActorId(postId: Long, actorId: Long): Unit = query {
        Reactions.deleteWhere {
            Reactions.postId eq postId and (Reactions.actorId eq actorId)
        }
    }

    override suspend fun deleteByPostIdAndActorIdAndEmoji(postId: Long, actorId: Long, emoji: Emoji): Unit = query {
        if (emoji is CustomEmoji) {
            Reactions.deleteWhere {
                Reactions.postId.eq(postId)
                    .and(Reactions.actorId.eq(actorId))
                    .and(Reactions.customEmojiId.eq(emoji.id))
            }
        } else {
            Reactions.deleteWhere {
                Reactions.postId.eq(postId)
                    .and(Reactions.actorId.eq(actorId))
                    .and(Reactions.unicodeEmoji.eq(emoji.name))
            }
        }
    }

    override suspend fun findByPostId(postId: Long): List<Reaction> = query {
        return@query Reactions.leftJoin(CustomEmojis).select { Reactions.postId eq postId }.map { it.toReaction() }
    }

    override suspend fun findByPostIdAndActorIdAndEmojiId(postId: Long, actorId: Long, emojiId: Long): Reaction? =
        query {
            return@query Reactions.leftJoin(CustomEmojis).select {
                Reactions.postId eq postId and (Reactions.actorId eq actorId).and(
                    Reactions.customEmojiId.eq(
                        emojiId
                    )
                )
            }.singleOrNull()?.toReaction()
        }

    override suspend fun existByPostIdAndActorIdAndEmojiId(postId: Long, actorId: Long, emojiId: Long): Boolean =
        query {
            return@query Reactions.select {
                Reactions.postId
                    .eq(postId)
                    .and(Reactions.actorId.eq(actorId))
                    .and(Reactions.customEmojiId.eq(emojiId))
            }.empty().not()
        }

    override suspend fun existByPostIdAndActorIdAndUnicodeEmoji(
        postId: Long,
        actorId: Long,
        unicodeEmoji: String
    ): Boolean = query {
        return@query Reactions.select {
            Reactions.postId
                .eq(postId)
                .and(Reactions.actorId.eq(actorId))
                .and(Reactions.unicodeEmoji.eq(unicodeEmoji))
        }.empty().not()
    }

    override suspend fun existByPostIdAndActorIdAndEmoji(postId: Long, actorId: Long, emoji: Emoji): Boolean = query {
        val query = Reactions.select {
            Reactions.postId
                .eq(postId)
                .and(Reactions.actorId.eq(actorId))
        }

        if (emoji is UnicodeEmoji) {
            query.andWhere { Reactions.unicodeEmoji eq emoji.name }
        } else {
            emoji as CustomEmoji
            query.andWhere { Reactions.customEmojiId eq emoji.id }
        }

        return@query query.empty().not()
    }

    override suspend fun existByPostIdAndActor(postId: Long, actorId: Long): Boolean = query {
        Reactions.select {
            Reactions.postId.eq(postId).and(Reactions.actorId.eq(actorId))
        }.empty().not()
    }

    override suspend fun findByPostIdAndActorId(postId: Long, actorId: Long): List<Reaction> = query {
        return@query Reactions.leftJoin(CustomEmojis)
            .select { Reactions.postId eq postId and (Reactions.actorId eq actorId) }
            .map { it.toReaction() }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReactionRepositoryImpl::class.java)
    }
}

fun ResultRow.toReaction(): Reaction {
    val emoji = if (this[Reactions.customEmojiId] != null) {
        CustomEmoji(
            id = this[Reactions.customEmojiId]!!,
            name = this[CustomEmojis.name],
            domain = this[CustomEmojis.domain],
            instanceId = this[CustomEmojis.instanceId],
            url = this[CustomEmojis.url],
            category = this[CustomEmojis.category],
            createdAt = this[CustomEmojis.createdAt]
        )
    } else if (this[Reactions.unicodeEmoji] != null) {
        UnicodeEmoji(this[Reactions.unicodeEmoji]!!)
    } else {
        throw IllegalStateException("customEmojiId and unicodeEmoji is null.")
    }

    return Reaction(
        this[Reactions.id].value,
        emoji,
        this[Reactions.postId],
        this[Reactions.actorId]
    )
}

object Reactions : LongIdTable("reactions") {
    val customEmojiId = long("custom_emoji_id").references(CustomEmojis.id).nullable()
    val unicodeEmoji = varchar("unicode_emoji", 255).nullable()
    val postId: Column<Long> =
        long("post_id").references(Posts.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val actorId: Column<Long> =
        long("actor_id").references(Actors.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

    init {
        uniqueIndex(customEmojiId, postId, actorId)
    }
}
