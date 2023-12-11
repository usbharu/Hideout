package dev.usbharu.hideout.core.domain.model.actor

import org.springframework.stereotype.Repository

@Repository
interface ActorRepository {
    suspend fun save(actor: Actor): Actor

    suspend fun findById(id: Long): Actor?

    suspend fun delete(id: Long)

    suspend fun nextId(): Long
}
