package dev.usbharu.hideout.core.service.user

data class UserCreateDto(
    val name: String,
    val screenName: String,
    val description: String,
    val password: String
)
