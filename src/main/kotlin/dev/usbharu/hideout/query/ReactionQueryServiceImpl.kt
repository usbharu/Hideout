package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.repository.Reactions
import dev.usbharu.hideout.repository.toReaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single

@Single
class ReactionQueryServiceImpl : ReactionQueryService {
    override suspend fun findByPostId(postId: Long): List<Reaction> {
        return Reactions.select {
            Reactions.postId.eq(postId)
        }.map { it.toReaction() }
    }

    override suspend fun findByPostIdAndUserIdAndEmojiId(postId: Long, userId: Long, emojiId: Long): Reaction {
        return Reactions
            .select {
                Reactions.postId.eq(postId).and(Reactions.userId.eq(userId)).and(
                    Reactions.emojiId.eq(emojiId)
                )
            }
            .single()
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
