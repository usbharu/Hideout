package dev.usbharu.hideout.service

import dev.usbharu.hideout.domain.model.UserAuthentication
import dev.usbharu.hideout.domain.model.UserAuthenticationEntity

interface IUserAuthService {
    fun hash(password: String): String

    suspend fun usernameAlreadyUse(username: String): Boolean
    suspend fun registerAccount(username: String, hash: String)

    suspend fun verifyAccount(username: String, password: String): Boolean

    suspend fun findByUserId(userId: Long): UserAuthenticationEntity

    suspend fun findByUsername(username: String): UserAuthenticationEntity
    suspend fun createAccount(userEntity: UserAuthentication): UserAuthenticationEntity
}
