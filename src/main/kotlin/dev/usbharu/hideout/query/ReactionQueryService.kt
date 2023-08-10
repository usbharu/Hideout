package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction

interface ReactionQueryService {
    suspend fun findByPostId(postId: Long): List<Reaction>

    @Suppress("FunctionMaxLength")
    suspend fun findByPostIdAndUserIdAndEmojiId(postId: Long, userId: Long, emojiId: Long): Reaction

    suspend fun reactionAlreadyExist(postId: Long, userId: Long, emojiId: Long): Boolean

    suspend fun deleteByPostIdAndUserId(postId: Long, userId: Long)
}
