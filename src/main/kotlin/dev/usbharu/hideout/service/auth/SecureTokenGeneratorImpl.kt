package dev.usbharu.hideout.service.auth

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*

@Component
class SecureTokenGeneratorImpl : SecureTokenGenerator {
    override fun generate(): String {

        val byteArray = ByteArray(16)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(byteArray)


        return Base64.getUrlEncoder().encodeToString(byteArray)
    }
}
