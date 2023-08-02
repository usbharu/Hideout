package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction

interface ReactionRepository {
    suspend fun generateId(): Long
    suspend fun save(reaction: Reaction): Reaction
    suspend fun reactionAlreadyExist(postId: Long, userId: Long, emojiId: Long): Boolean
    suspend fun findByPostId(postId: Long): List<Reaction>
    suspend fun delete(reaction: Reaction):Reaction
    suspend fun deleteById(id:Long)
    suspend fun deleteByPostId(postId:Long)
    suspend fun deleteByUserId(userId: Long)
    suspend fun deleteByPostIdAndUserId(postId: Long,userId: Long)
}
