package dev.usbharu.hideout.core.query

import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import org.springframework.stereotype.Repository

@Repository
interface ReactionQueryService {
    suspend fun findByPostId(postId: Long, userId: Long? = null): List<Reaction>

    @Suppress("FunctionMaxLength")
    suspend fun findByPostIdAndUserIdAndEmojiId(postId: Long, userId: Long, emojiId: Long): Reaction

    suspend fun reactionAlreadyExist(postId: Long, userId: Long, emojiId: Long): Boolean

    suspend fun deleteByPostIdAndUserId(postId: Long, userId: Long)

}
