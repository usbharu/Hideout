package dev.usbharu.hideout.core.infrastructure.springframework.oauth2

import org.springframework.stereotype.Component

@Component
interface SecureTokenGenerator {
    fun generate(): String
}
