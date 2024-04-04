package dev.usbharu.hideout.application.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("hideout.security")
data class CaptchaConfig(
    val reCaptchaSiteKey:String
)
