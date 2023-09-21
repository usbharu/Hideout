package dev.usbharu.hideout.service.auth

import org.springframework.stereotype.Component

@Component
interface SecureTokenGenerator {
    fun generate(): String
}
