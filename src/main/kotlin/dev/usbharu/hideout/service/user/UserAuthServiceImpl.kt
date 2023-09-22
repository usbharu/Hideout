package dev.usbharu.hideout.service.user

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.query.UserQueryService
import org.koin.core.annotation.Single
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.security.*
import java.util.*

@Single
@Service
class UserAuthServiceImpl(
    val userQueryService: UserQueryService
) : UserAuthService {

    override fun hash(password: String): String {
        return BCryptPasswordEncoder().encode(password)
    }

    override suspend fun usernameAlreadyUse(username: String): Boolean {
        userQueryService.findByName(username)
        return true
    }

    override suspend fun verifyAccount(username: String, password: String): Boolean {
        val userEntity = userQueryService.findByNameAndDomain(username, Config.configData.domain)
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
        Base64.getEncoder().encodeToString(encoded).chunked(UserAuthServiceImpl.pemSize).joinToString("\n") +
        "\n-----END PUBLIC KEY-----\n"
}

fun PrivateKey.toPem(): String {
    return "-----BEGIN PRIVATE KEY-----\n" +
        Base64.getEncoder().encodeToString(encoded).chunked(UserAuthServiceImpl.pemSize).joinToString("\n") +
        "\n-----END PRIVATE KEY-----\n"
}
