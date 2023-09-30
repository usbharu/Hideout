package dev.usbharu.hideout.service.user

import org.springframework.stereotype.Service
import java.security.KeyPair

@Service
interface UserAuthService {
    fun hash(password: String): String

    suspend fun usernameAlreadyUse(username: String): Boolean

    suspend fun generateKeyPair(): KeyPair
}
