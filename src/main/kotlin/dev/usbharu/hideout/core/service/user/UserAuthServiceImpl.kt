package dev.usbharu.hideout.core.service.user

import dev.usbharu.hideout.core.query.ActorQueryService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.security.*
import java.util.*

@Service
class UserAuthServiceImpl(
    val actorQueryService: ActorQueryService
) : UserAuthService {

    override fun hash(password: String): String = BCryptPasswordEncoder().encode(password)

    override suspend fun usernameAlreadyUse(username: String): Boolean {
        actorQueryService.findByName(username)
        return true
    }

    override suspend fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(keySize)
        return keyPairGenerator.generateKeyPair()
    }

    companion object {
        val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")
        const val keySize: Int = 2048
        const val pemSize: Int = 64
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
