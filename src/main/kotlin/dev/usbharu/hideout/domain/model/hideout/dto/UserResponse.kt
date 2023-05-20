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
                id = user.id,
                name = user.name,
                domain = user.domain,
                screenName = user.screenName,
                description = user.description,
                url = user.url,
                createdAt = user.createdAt.toEpochMilli()
            )
        }
    }
}
