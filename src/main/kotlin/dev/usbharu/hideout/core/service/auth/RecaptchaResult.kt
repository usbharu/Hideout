package dev.usbharu.hideout.core.service.auth

data class RecaptchaResult(
    val success: Boolean,
    val challenge_ts: String,
    val hostname: String,
    val score: Float,
    val action: String
)
