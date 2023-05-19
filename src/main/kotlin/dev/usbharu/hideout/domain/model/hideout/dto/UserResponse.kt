package dev.usbharu.hideout.domain.model.hideout.dto

data class UserResponse(
    val id: Long,
    val name: String,
    val domain: String,
    val screenName: String,
    val description: String = "",
    val url: String,
    val createdAt: Long
)
