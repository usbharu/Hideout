package dev.usbharu.hideout.service.user

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.repository.IUserRepository
import io.ktor.util.*
import org.koin.core.annotation.Single
import java.security.*
import java.util.*

@Single
class UserAuthService(
    val userRepository: IUserRepository
) : IUserAuthService {

    override fun hash(password: String): String {
        val digest = sha256.digest(password.toByteArray(Charsets.UTF_8))
        return hex(digest)
    }

    override suspend fun usernameAlreadyUse(username: String): Boolean {
        userRepository.findByName(username)
        return true
    }

    override suspend fun verifyAccount(username: String, password: String): Boolean {
        val userEntity = userRepository.findByNameAndDomain(username, Config.configData.domain)
            ?: return false
        return userEntity.password == hash(password)
    }

    override suspend fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(keySize)
        return keyPairGenerator.generateKeyPair()
    }

    companion object {
        val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")
        const val keySize = 2048
        const val pemSize = 64
    }
}

fun PublicKey.toPem(): String {
    return "-----BEGIN PUBLIC KEY-----\n" +
        Base64.getEncoder().encodeToString(encoded).chunked(UserAuthService.pemSize).joinToString("\n") +
        "\n-----END PUBLIC KEY-----\n"
}

fun PrivateKey.toPem(): String {
    return "-----BEGIN PRIVATE KEY-----\n" +
        Base64.getEncoder().encodeToString(encoded).chunked(UserAuthService.pemSize).joinToString("\n") +
        "\n-----END PRIVATE KEY-----\n"
}
