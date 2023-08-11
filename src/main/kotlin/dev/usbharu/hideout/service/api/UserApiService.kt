package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.Acct
import dev.usbharu.hideout.domain.model.hideout.dto.UserCreateDto
import dev.usbharu.hideout.domain.model.hideout.dto.UserResponse
import dev.usbharu.hideout.exception.UsernameAlreadyExistException
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.user.UserService
import org.koin.core.annotation.Single
import kotlin.math.min

interface UserApiService {
    suspend fun findAll(limit: Int? = 100, offset: Long = 0): List<UserResponse>

    suspend fun findById(id: Long): UserResponse

    suspend fun findByIds(ids: List<Long>): List<UserResponse>

    suspend fun findByAcct(acct: Acct): UserResponse

    suspend fun findFollowers(userId: Long): List<UserResponse>

    suspend fun findFollowings(userId: Long): List<UserResponse>

    suspend fun findFollowersByAcct(acct: Acct): List<UserResponse>

    suspend fun findFollowingsByAcct(acct: Acct): List<UserResponse>

    suspend fun createUser(username: String, password: String): UserResponse
}

@Single
class UserApiServiceImpl(
    private val userQueryService: UserQueryService,
    private val followerQueryService: FollowerQueryService,
    private val userService: UserService,
    private val transaction: Transaction
) : UserApiService {
    override suspend fun findAll(limit: Int?, offset: Long): List<UserResponse> =
        userQueryService.findAll(min(limit ?: 100, 100), offset).map { UserResponse.from(it) }

    override suspend fun findById(id: Long): UserResponse = UserResponse.from(userQueryService.findById(id))

    override suspend fun findByIds(ids: List<Long>): List<UserResponse> =
        userQueryService.findByIds(ids).map { UserResponse.from(it) }

    override suspend fun findByAcct(acct: Acct): UserResponse =
        UserResponse.from(userQueryService.findByNameAndDomain(acct.username, acct.domain ?: Config.configData.domain))

    override suspend fun findFollowers(userId: Long): List<UserResponse> =
        followerQueryService.findFollowersById(userId).map { UserResponse.from(it) }

    override suspend fun findFollowings(userId: Long): List<UserResponse> =
        followerQueryService.findFollowingById(userId).map { UserResponse.from(it) }

    override suspend fun findFollowersByAcct(acct: Acct): List<UserResponse> =
        followerQueryService.findFollowersByNameAndDomain(acct.username, acct.domain ?: Config.configData.domain)
            .map { UserResponse.from(it) }

    override suspend fun findFollowingsByAcct(acct: Acct): List<UserResponse> =
        followerQueryService.findFollowingByNameAndDomain(acct.username, acct.domain ?: Config.configData.domain)
            .map { UserResponse.from(it) }

    override suspend fun createUser(username: String, password: String): UserResponse {
        return transaction.transaction {
            if (userQueryService.existByNameAndDomain(username, Config.configData.domain)) {
                throw UsernameAlreadyExistException()
            }
            UserResponse.from(userService.createLocalUser(UserCreateDto(username, username, "", password)))
        }
    }
}
