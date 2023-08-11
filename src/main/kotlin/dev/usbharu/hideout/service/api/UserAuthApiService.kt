package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.domain.model.hideout.dto.JwtToken
import dev.usbharu.hideout.domain.model.hideout.form.RefreshToken

interface UserAuthApiService {
    suspend fun login(username: String, password: String): JwtToken
    suspend fun refreshToken(refreshToken: RefreshToken): JwtToken
}
