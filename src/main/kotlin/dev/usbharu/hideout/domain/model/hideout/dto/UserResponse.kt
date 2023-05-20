package dev.usbharu.hideout.domain.model.hideout.dto

import dev.usbharu.hideout.domain.model.hideout.entity.User

data class UserResponse(
    val id: Long,
    val name: String,
    val domain: String,
    val screenName: String,
    val description: String = "",
    val url: String,
    val createdAt: Long
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                user.id,
                user.name,
                user.domain,
                user.screenName,
                user.description,
                user.url,
                user.createdAt.toEpochMilli()
            )
        }
    }
}
