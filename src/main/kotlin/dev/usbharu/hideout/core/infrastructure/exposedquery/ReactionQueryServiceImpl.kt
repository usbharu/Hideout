package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.query.ReactionQueryService
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Reactions
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toReaction
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository

@Repository
class ReactionQueryServiceImpl : ReactionQueryService {
    override suspend fun findByPostId(postId: Long, userId: Long?): List<Reaction> {
        return Reactions.select {
            Reactions.postId.eq(postId)
        }.map { it.toReaction() }
    }

    @Suppress("FunctionMaxLength")
    override suspend fun findByPostIdAndUserIdAndEmojiId(postId: Long, userId: Long, emojiId: Long): Reaction {
        return Reactions
            .select {
                Reactions.postId.eq(postId).and(Reactions.userId.eq(userId)).and(
                    Reactions.emojiId.eq(emojiId)
                )
            }
            .singleOr {
                FailedToGetResourcesException(
                    "postId: $postId,userId: $userId,emojiId: $emojiId is duplicate or does not exist.",
                    it
                )
            }
            .toReaction()
    }

    override suspend fun reactionAlreadyExist(postId: Long, userId: Long, emojiId: Long): Boolean {
        return Reactions.select {
            Reactions.postId.eq(postId).and(Reactions.userId.eq(userId)).and(
                Reactions.emojiId.eq(emojiId)
            )
        }.empty().not()
    }

    override suspend fun deleteByPostIdAndUserId(postId: Long, userId: Long) {
        Reactions.deleteWhere { Reactions.postId.eq(postId).and(Reactions.userId.eq(userId)) }
    }

}
