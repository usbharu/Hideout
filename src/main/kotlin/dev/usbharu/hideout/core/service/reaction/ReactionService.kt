package dev.usbharu.hideout.core.service.reaction

import org.springframework.stereotype.Service

@Service
interface ReactionService {
    suspend fun receiveReaction(name: String, domain: String, userId: Long, postId: Long)
    suspend fun sendReaction(name: String, userId: Long, postId: Long)
    suspend fun removeReaction(userId: Long, postId: Long)
}