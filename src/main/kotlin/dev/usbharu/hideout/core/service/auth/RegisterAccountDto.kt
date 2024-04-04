package dev.usbharu.hideout.core.service.auth

data class RegisterAccountDto(
    val username:String,
    val password:String,
    val recaptchaResponse:String
)
