package dev.usbharu.hideout.service.reaction

interface IReactionService {
    suspend fun receiveReaction(name: String, domain: String, userId: Long, postId: Long)
    suspend fun sendReaction(name: String, userId: Long, postId: Long)
}
