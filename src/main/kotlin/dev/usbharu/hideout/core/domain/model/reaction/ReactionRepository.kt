package dev.usbharu.hideout.core.domain.model.reaction

import org.springframework.stereotype.Repository

@Repository
interface ReactionRepository {
    suspend fun generateId(): Long
    suspend fun save(reaction: Reaction): Reaction
    suspend fun delete(reaction: Reaction): Reaction
    suspend fun deleteByPostId(postId: Long): Int
    suspend fun deleteByActorId(actorId: Long): Int
    suspend fun findByPostId(postId: Long): List<Reaction>
    suspend fun findByPostIdAndActorIdAndEmojiId(postId: Long, actorId: Long, emojiId: Long): Reaction?
    suspend fun existByPostIdAndActorIdAndEmojiId(postId: Long, actorId: Long, emojiId: Long): Boolean
    suspend fun findByPostIdAndActorId(postId: Long, actorId: Long): List<Reaction>
}
