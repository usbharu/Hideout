package dev.usbharu.hideout.service.reaction

import dev.usbharu.hideout.domain.model.hideout.dto.ReactionResponse

interface IReactionService {
    suspend fun receiveReaction(name: String, domain: String, userId: Long, postId: Long)
    suspend fun sendReaction(name: String, userId: Long, postId: Long)
    suspend fun removeReaction(userId: Long, postId: Long)
    suspend fun findByPostIdForUser(postId: Long, userId: Long): List<ReactionResponse>
}
