package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.dto.ReactionResponse
import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import org.springframework.stereotype.Repository

@Repository
interface ReactionQueryService {
    suspend fun findByPostId(postId: Long, userId: Long? = null): List<Reaction>

    @Suppress("FunctionMaxLength")
    suspend fun findByPostIdAndUserIdAndEmojiId(postId: Long, userId: Long, emojiId: Long): Reaction

    suspend fun reactionAlreadyExist(postId: Long, userId: Long, emojiId: Long): Boolean

    suspend fun deleteByPostIdAndUserId(postId: Long, userId: Long)

    suspend fun findByPostIdWithUsers(postId: Long, userId: Long? = null): List<ReactionResponse>
}
