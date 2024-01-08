package dev.usbharu.hideout.core.service.reaction

import dev.usbharu.hideout.core.domain.model.emoji.Emoji
import org.springframework.stereotype.Service

@Service
interface ReactionService {
    suspend fun receiveReaction(emoji: Emoji, actorId: Long, postId: Long)
    suspend fun receiveRemoveReaction(actorId: Long, postId: Long)
    suspend fun sendReaction(emoji: Emoji, actorId: Long, postId: Long)
    suspend fun removeReaction(actorId: Long, postId: Long)
}
