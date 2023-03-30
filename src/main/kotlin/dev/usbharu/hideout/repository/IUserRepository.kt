package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.domain.model.UserEntity

interface IUserRepository {
    suspend fun create(user: User): UserEntity

    suspend fun findById(id: Long): UserEntity?

    suspend fun findByName(name: String): UserEntity?

    suspend fun update(userEntity: UserEntity)

    suspend fun delete(id: Long)

    suspend fun findAll(): List<User>

    suspend fun findAllByLimitAndByOffset(limit: Int, offset: Long = 0): List<UserEntity>

    suspend fun createFollower(id: Long, follower: Long)
    suspend fun deleteFollower(id: Long, follower: Long)
    suspend fun findFollowersById(id: Long): List<UserEntity>
}
