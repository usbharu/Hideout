package dev.usbharu.hideout.domain.model.hideout.dto

data class UserCreateDto(
    val name:String,
    val domain:String,
    val screenName:String,
    val description:String,
    val password:String
)
