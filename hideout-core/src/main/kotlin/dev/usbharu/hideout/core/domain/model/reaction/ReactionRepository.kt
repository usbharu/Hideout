package dev.usbharu.hideout.core.domain.model.reaction

interface ReactionRepository {
    suspend fun save(reaction: Reaction): Reaction
    suspend fun findById(reactionId: String): Reaction?
    suspend fun delete(reaction: Reaction)
}