package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.JwtToken
import dev.usbharu.hideout.domain.model.hideout.form.RefreshToken
import dev.usbharu.hideout.exception.InvalidUsernameOrPasswordException
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.auth.IJwtService
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.user.UserAuthService
import org.koin.core.annotation.Single

interface UserAuthApiService {
    suspend fun login(username: String, password: String): JwtToken
    suspend fun refreshToken(refreshToken: RefreshToken): JwtToken
}

@Single
class UserAuthApiServiceImpl(
    private val userAuthService: UserAuthService,
    private val userQueryService: UserQueryService,
    private val jwtService: IJwtService,
    private val transaction: Transaction
) : UserAuthApiService {
    override suspend fun login(username: String, password: String): JwtToken {
        return transaction.transaction {
            if (userAuthService.verifyAccount(username, password).not()) {
                throw InvalidUsernameOrPasswordException()
            }
            val user = userQueryService.findByNameAndDomain(username, Config.configData.domain)
            jwtService.createToken(user)
        }
    }

    override suspend fun refreshToken(refreshToken: RefreshToken): JwtToken {
        return transaction.transaction {
            jwtService.refreshToken(refreshToken)
        }
    }
}
