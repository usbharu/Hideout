package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.User

interface IUserRepository {
    suspend fun create(user: User): User

    suspend fun findById(id: Long): User?

    suspend fun findByIds(ids: List<Long>): List<User>

    suspend fun findByName(name: String): User?

    suspend fun findByNameAndDomains(names: List<Pair<String,String>>): List<User>

    suspend fun findByUrl(url:String): User?

    suspend fun findByUrls(urls: List<String>): List<User>

    suspend fun update(userEntity: User)

    suspend fun delete(id: Long)

    suspend fun findAll(): List<User>

    suspend fun findAllByLimitAndByOffset(limit: Int, offset: Long = 0): List<User>

    suspend fun createFollower(id: Long, follower: Long)
    suspend fun deleteFollower(id: Long, follower: Long)
    suspend fun findFollowersById(id: Long): List<User>
}
