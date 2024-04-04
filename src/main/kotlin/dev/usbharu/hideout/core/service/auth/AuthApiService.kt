package dev.usbharu.hideout.core.service.auth

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail

interface AuthApiService {
    suspend fun registerAccount(registerAccountDto: RegisterAccountDto): Actor
}