package dev.usbharu.hideout.core.domain.model.user

import org.springframework.stereotype.Repository

@Repository
interface UserRepository {
    suspend fun save(user: User): User

    suspend fun findById(id: Long): User?

    suspend fun delete(id: Long)

    suspend fun nextId(): Long
}
