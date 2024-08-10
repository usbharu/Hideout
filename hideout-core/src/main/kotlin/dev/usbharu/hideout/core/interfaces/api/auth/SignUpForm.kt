package dev.usbharu.hideout.core.interfaces.api.auth

data class SignUpForm(
    val username: String,
    val password: String,
//    val recaptchaResponse: String
)
