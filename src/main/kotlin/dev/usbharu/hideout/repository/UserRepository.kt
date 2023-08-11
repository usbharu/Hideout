package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.User

@Suppress("TooManyFunctions")
interface UserRepository {
    suspend fun save(user: User): User

    suspend fun findById(id: Long): User?

    suspend fun delete(id: Long)

    suspend fun deleteFollowRequest(id: Long, follower: Long)

    suspend fun findFollowRequestsById(id: Long, follower: Long): Boolean

    suspend fun nextId(): Long
}
