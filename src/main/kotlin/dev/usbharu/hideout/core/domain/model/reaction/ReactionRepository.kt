package dev.usbharu.hideout.core.domain.model.reaction

import org.springframework.stereotype.Repository

@Repository
interface ReactionRepository {
    suspend fun generateId(): Long
    suspend fun save(reaction: Reaction): Reaction
    suspend fun delete(reaction: Reaction): Reaction
    suspend fun deleteByPostId(postId: Long): Int
    suspend fun deleteByActorId(actorId: Long): Int
}
