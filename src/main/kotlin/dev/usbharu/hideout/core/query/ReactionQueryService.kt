package dev.usbharu.hideout.core.query

import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import org.springframework.stereotype.Repository

@Repository
interface ReactionQueryService {
    suspend fun findByPostId(postId: Long, actorId: Long? = null): List<Reaction>

    @Suppress("FunctionMaxLength")
    suspend fun findByPostIdAndActorIdAndEmojiId(postId: Long, actorId: Long, emojiId: Long): Reaction

    suspend fun reactionAlreadyExist(postId: Long, actorId: Long, emojiId: Long): Boolean

    suspend fun findByPostIdAndActorId(postId: Long, actorId: Long): List<Reaction>
}
