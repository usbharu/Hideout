package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Reactions
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toReaction
import dev.usbharu.hideout.core.query.ReactionQueryService
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository

@Repository
class ReactionQueryServiceImpl : ReactionQueryService {
    override suspend fun findByPostId(postId: Long, actorId: Long?): List<Reaction> {
        return Reactions.select {
            Reactions.postId.eq(postId)
        }.map { it.toReaction() }
    }

    @Suppress("FunctionMaxLength")
    override suspend fun findByPostIdAndActorIdAndEmojiId(postId: Long, actorId: Long, emojiId: Long): Reaction? {
        return Reactions
            .select {
                Reactions.postId.eq(postId).and(Reactions.actorId.eq(actorId)).and(
                    Reactions.emojiId.eq(emojiId)
                )
            }
            .singleOrNull()
            ?.toReaction()
    }

    override suspend fun reactionAlreadyExist(postId: Long, actorId: Long, emojiId: Long): Boolean {
        return Reactions.select {
            Reactions.postId.eq(postId).and(Reactions.actorId.eq(actorId)).and(
                Reactions.emojiId.eq(emojiId)
            )
        }.empty().not()
    }

    override suspend fun findByPostIdAndActorId(postId: Long, actorId: Long): List<Reaction> {
        return Reactions.select { Reactions.postId eq postId and (Reactions.actorId eq actorId) }
            .map { it.toReaction() }
    }
}
