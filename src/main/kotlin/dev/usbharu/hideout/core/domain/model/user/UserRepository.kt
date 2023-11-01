package dev.usbharu.hideout.core.domain.model.user

import org.springframework.stereotype.Repository

@Suppress("TooManyFunctions")
@Repository
interface UserRepository {
    suspend fun save(user: User): User

    suspend fun findById(id: Long): User?

    suspend fun delete(id: Long)

    suspend fun deleteFollowRequest(id: Long, follower: Long)

    suspend fun findFollowRequestsById(id: Long, follower: Long): Boolean

    suspend fun nextId(): Long
}
