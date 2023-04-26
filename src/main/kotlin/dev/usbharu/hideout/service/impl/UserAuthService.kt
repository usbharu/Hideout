package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.IUserAuthService
import io.ktor.util.*
import java.security.*
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

class UserAuthService(
    val userRepository: IUserRepository
) : IUserAuthService {


    override fun hash(password: String): String {
        val digest = sha256.digest(password.toByteArray(Charsets.UTF_8))
        return hex(digest)
    }

    override suspend fun usernameAlreadyUse(username: String): Boolean {
        userRepository.findByName(username) ?: return false
        return true
    }

    @Deprecated("")
    override suspend fun registerAccount(username: String, hash: String) {
        val url = "${Config.configData.url}/users/$username"
        val registerUser = User(
            id = 0L,
            name = username,
            domain = Config.configData.domain,
            screenName = username,
            description = "",
            inbox = "$url/inbox",
            outbox = "$url/outbox",
            url = url,
            publicKey = "",
            createdAt = Instant.now(),
        )
        val createdUser = userRepository.create(registerUser)

        val keyPair = generateKeyPair()
        val privateKey = keyPair.private as RSAPrivateKey
        val publicKey = keyPair.public as RSAPublicKey

        TODO()
    }

    override suspend fun verifyAccount(username: String, password: String): Boolean {
        val userEntity = userRepository.findByName(username)
            ?: throw UserNotFoundException("$username was not found")
        return userEntity.password == hash(password)
    }

    private fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(1024)
        return keyPairGenerator.generateKeyPair()
    }


    companion object {
        val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")
    }
}

fun PublicKey.toPem(): String {
    return "-----BEGIN PUBLIC KEY-----\n" +
            Base64.getEncoder().encodeToString(encoded).chunked(64).joinToString("\n") +
            "\n-----END PUBLIC KEY-----\n"
}

fun PrivateKey.toPem(): String {
    return "-----BEGIN PRIVATE KEY-----" +
            Base64.getEncoder().encodeToString(encoded).chunked(64).joinToString("\n") +
            "\n-----END PRIVATE KEY-----\n"
}
