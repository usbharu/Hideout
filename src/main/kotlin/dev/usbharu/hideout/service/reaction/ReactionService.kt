package dev.usbharu.hideout.service.reaction

interface ReactionService {
    suspend fun receiveReaction(name: String, domain: String, userId: Long, postId: Long)
    suspend fun sendReaction(name: String, userId: Long, postId: Long)
    suspend fun removeReaction(userId: Long, postId: Long)
}
