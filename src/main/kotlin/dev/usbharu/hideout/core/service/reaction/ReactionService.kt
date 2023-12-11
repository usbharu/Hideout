package dev.usbharu.hideout.core.service.reaction

import org.springframework.stereotype.Service

@Service
interface ReactionService {
    suspend fun receiveReaction(name: String, domain: String, actorId: Long, postId: Long)
    suspend fun sendReaction(name: String, actorId: Long, postId: Long)
    suspend fun removeReaction(actorId: Long, postId: Long)
}
