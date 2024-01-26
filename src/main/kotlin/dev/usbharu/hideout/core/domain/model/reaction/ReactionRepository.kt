package dev.usbharu.hideout.core.domain.model.reaction

import dev.usbharu.hideout.core.domain.model.emoji.Emoji
import org.springframework.stereotype.Repository

@Repository
@Suppress("FunctionMaxLength", "TooManyFunction")
interface ReactionRepository {
    suspend fun generateId(): Long
    suspend fun save(reaction: Reaction): Reaction
    suspend fun delete(reaction: Reaction): Reaction
    suspend fun deleteByPostId(postId: Long): Int
    suspend fun deleteByActorId(actorId: Long): Int
    suspend fun deleteByPostIdAndActorId(postId: Long, actorId: Long)
    suspend fun deleteByPostIdAndActorIdAndEmoji(postId: Long, actorId: Long, emoji: Emoji)
    suspend fun findById(id: Long): Reaction?
    suspend fun findByPostId(postId: Long): List<Reaction>
    suspend fun findByPostIdAndActorIdAndEmojiId(postId: Long, actorId: Long, emojiId: Long): Reaction?
    suspend fun existByPostIdAndActorIdAndEmojiId(postId: Long, actorId: Long, emojiId: Long): Boolean
    suspend fun existByPostIdAndActorIdAndUnicodeEmoji(postId: Long, actorId: Long, unicodeEmoji: String): Boolean
    suspend fun existByPostIdAndActorIdAndEmoji(postId: Long, actorId: Long, emoji: Emoji): Boolean
    suspend fun existByPostIdAndActor(postId: Long, actorId: Long): Boolean
    suspend fun findByPostIdAndActorId(postId: Long, actorId: Long): List<Reaction>
}
