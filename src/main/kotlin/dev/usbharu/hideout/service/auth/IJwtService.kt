package dev.usbharu.hideout.service.auth

import dev.usbharu.hideout.domain.model.hideout.dto.JwtToken
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.hideout.form.RefreshToken

interface IJwtService {
    suspend fun createToken(user: User): JwtToken
    suspend fun refreshToken(refreshToken: RefreshToken): JwtToken

    suspend fun revokeToken(refreshToken: RefreshToken)
    suspend fun revokeToken(user: User)
    suspend fun revokeAll()
}
