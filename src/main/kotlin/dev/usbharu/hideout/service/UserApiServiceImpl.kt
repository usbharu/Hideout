package dev.usbharu.hideout.service

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.Acct
import dev.usbharu.hideout.domain.model.hideout.dto.UserResponse
import dev.usbharu.hideout.service.impl.IUserService
import org.koin.core.annotation.Single

@Single
class UserApiServiceImpl(private val userService: IUserService) : IUserApiService {
    override suspend fun findAll(limit: Int?, offset: Long): List<UserResponse> =
        userService.findAll(limit, offset).map { UserResponse.from(it) }

    override suspend fun findById(id: Long): UserResponse = UserResponse.from(userService.findById(id))

    override suspend fun findByIds(ids: List<Long>): List<UserResponse> =
        userService.findByIds(ids).map { UserResponse.from(it) }

    override suspend fun findByAcct(acct: Acct): UserResponse =
        UserResponse.from(userService.findByNameAndDomain(acct.username, acct.domain))

    override suspend fun findByAccts(accts: List<Acct>): List<UserResponse> {
        return userService.findByNameAndDomains(accts.map { it.username to (it.domain ?: Config.configData.domain) })
            .map { UserResponse.from(it) }
    }

    override suspend fun findFollowers(userId: Long): List<UserResponse> =
        userService.findFollowersById(userId).map { UserResponse.from(it) }

    override suspend fun findFollowings(userId: Long): List<UserResponse> =
        userService.findFollowingById(userId).map { UserResponse.from(it) }

    override suspend fun findFollowersByAcct(acct: Acct): List<UserResponse> =
        userService.findFollowersByNameAndDomain(acct.username, acct.domain).map { UserResponse.from(it) }

    override suspend fun findFollowingsByAcct(acct: Acct): List<UserResponse> =
        userService.findFollowingByNameAndDomain(acct.username, acct.domain).map { UserResponse.from(it) }
}
