package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.Acct
import dev.usbharu.hideout.domain.model.hideout.dto.UserCreateDto
import dev.usbharu.hideout.domain.model.hideout.dto.UserResponse
import dev.usbharu.hideout.exception.UsernameAlreadyExistException
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.user.IUserService
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import kotlin.math.min

@Single
class UserApiServiceImpl(
    private val userQueryService: UserQueryService,
    private val followerQueryService: FollowerQueryService,
    private val userService: IUserService
) : IUserApiService {
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
        return newSuspendedTransaction {
            if (userQueryService.existByNameAndDomain(username, Config.configData.domain)) {
                throw UsernameAlreadyExistException()
            }
            UserResponse.from(userService.createLocalUser(UserCreateDto(username, username, "", password)))
        }
    }
}
