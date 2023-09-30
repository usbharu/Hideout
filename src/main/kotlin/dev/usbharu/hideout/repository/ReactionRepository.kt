package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import org.springframework.stereotype.Repository

@Repository
interface ReactionRepository {
    suspend fun save(reaction: Reaction): Reaction
    suspend fun delete(reaction: Reaction): Reaction
}
