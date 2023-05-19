package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.domain.model.hideout.dto.RemoteUserCreateDto
import dev.usbharu.hideout.domain.model.hideout.dto.UserCreateDto
import dev.usbharu.hideout.domain.model.hideout.dto.UserResponse
import dev.usbharu.hideout.domain.model.hideout.entity.User

@Suppress("TooManyFunctions")
interface IUserService {
    suspend fun findAll(limit: Int? = 100, offset: Long? = 0): List<User>

    suspend fun findAllForUser(limit: Int? = 100, offset: Long? = 0): List<UserResponse>

    suspend fun findById(id: Long): User

    suspend fun findByIds(ids: List<Long>): List<User>

    suspend fun findByName(name: String): List<User>

    suspend fun findByNameLocalUser(name: String): User

    suspend fun findByNameAndDomain(name: String, domain: String? = null): User

    suspend fun findByNameAndDomains(names: List<Pair<String, String>>): List<User>

    suspend fun findByUrl(url: String): User

    suspend fun findByUrls(urls: List<String>): List<User>

    suspend fun usernameAlreadyUse(username: String): Boolean

    suspend fun createLocalUser(user: UserCreateDto): User

    suspend fun createRemoteUser(user: RemoteUserCreateDto): User

    suspend fun findFollowersById(id: Long): List<User>

    suspend fun findFollowersByNameAndDomain(name: String, domain: String?): List<User>

    suspend fun findFollowingById(id: Long): List<User>

    suspend fun findFollowingByNameAndDomain(name: String, domain: String?): List<User>

    /**
     * フォロワーを追加する
     *
     * @param id
     * @param follower
     * @return リクエストが成功したか
     */
    suspend fun addFollowers(id: Long, follower: Long): Boolean
}
