package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Reactions
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toReaction
import dev.usbharu.hideout.core.query.ReactionQueryService
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
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
    override suspend fun findByPostIdAndActorIdAndEmojiId(postId: Long, actorId: Long, emojiId: Long): Reaction {
        return Reactions
            .select {
                Reactions.postId.eq(postId).and(Reactions.actorId.eq(actorId)).and(
                    Reactions.emojiId.eq(emojiId)
                )
            }
            .singleOr {
                FailedToGetResourcesException(
                    "postId: $postId,userId: $actorId,emojiId: $emojiId is duplicate or does not exist.",
                    it
                )
            }
            .toReaction()
    }

    override suspend fun reactionAlreadyExist(postId: Long, actorId: Long, emojiId: Long): Boolean {
        return Reactions.select {
            Reactions.postId.eq(postId).and(Reactions.actorId.eq(actorId)).and(
                Reactions.emojiId.eq(emojiId)
            )
        }.empty().not()
    }

    override suspend fun deleteByPostIdAndActorId(postId: Long, actorId: Long) {
        Reactions.deleteWhere { Reactions.postId.eq(postId).and(Reactions.actorId.eq(actorId)) }
    }
}
