package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.service.id.IdGenerateService
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

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(reaction: Reaction): Reaction = query {
        if (Reactions.select { Reactions.id eq reaction.id }.forUpdate().empty()) {
            Reactions.insert {
                it[id] = reaction.id
                it[emojiId] = reaction.emojiId
                it[postId] = reaction.postId
                it[actorId] = reaction.actorId
            }
        } else {
            Reactions.update({ Reactions.id eq reaction.id }) {
                it[emojiId] = reaction.emojiId
                it[postId] = reaction.postId
                it[actorId] = reaction.actorId
            }
        }
        return@query reaction
    }

    override suspend fun delete(reaction: Reaction): Reaction = query {
        Reactions.deleteWhere {
            id.eq(reaction.id).and(postId.eq(reaction.postId)).and(actorId.eq(reaction.actorId))
                .and(emojiId.eq(reaction.emojiId))
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

    override suspend fun findByPostId(postId: Long): List<Reaction> = query {
        return@query Reactions.select { Reactions.postId eq postId }.map { it.toReaction() }
    }

    override suspend fun findByPostIdAndActorIdAndEmojiId(postId: Long, actorId: Long, emojiId: Long): Reaction? =
        query {
            return@query Reactions.select {
                Reactions.postId eq postId and (Reactions.actorId eq actorId).and(
                    Reactions.emojiId.eq(
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
                    .and(Reactions.emojiId.eq(emojiId))
            }.empty().not()
        }

    override suspend fun findByPostIdAndActorId(postId: Long, actorId: Long): List<Reaction> = query {
        return@query Reactions.select { Reactions.postId eq postId and (Reactions.actorId eq actorId) }
            .map { it.toReaction() }
    }

    override val logger: Logger
        get() = Companion.logger

    companion object {
        private val logger = LoggerFactory.getLogger(ReactionRepositoryImpl::class.java)
    }
}

fun ResultRow.toReaction(): Reaction {
    return Reaction(
        this[Reactions.id].value, this[Reactions.emojiId], this[Reactions.postId], this[Reactions.actorId]
    )
}

object Reactions : LongIdTable("reactions") {
    val emojiId: Column<Long> = long("emoji_id")
    val postId: Column<Long> =
        long("post_id").references(Posts.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val actorId: Column<Long> =
        long("actor_id").references(Actors.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

    init {
        uniqueIndex(emojiId, postId, actorId)
    }
}
