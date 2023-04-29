package dev.usbharu.hideout.service

import java.security.KeyPair

interface IUserAuthService {
    fun hash(password: String): String

    suspend fun usernameAlreadyUse(username: String): Boolean

    suspend fun generateKeyPair(): KeyPair

    suspend fun verifyAccount(username: String, password: String): Boolean
}
