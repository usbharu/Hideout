package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction

interface ReactionRepository {
    suspend fun generateId(): Long
    suspend fun save(reaction: Reaction): Reaction
    suspend fun delete(reaction: Reaction): Reaction
}
