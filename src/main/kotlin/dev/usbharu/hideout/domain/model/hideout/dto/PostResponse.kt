package dev.usbharu.hideout.domain.model.hideout.dto

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility

data class PostResponse(
    val id: String,
    val user: UserResponse,
    val overview: String? = null,
    val text: String? = null,
    val createdAt: Long,
    val visibility: Visibility,
    val url: String,
    val sensitive: Boolean = false,
) {
    companion object {
        fun from(post: Post, user: User): PostResponse {
            return PostResponse(
                id = post.id.toString(),
                user = UserResponse.from(user),
                overview = post.overview,
                text = post.text,
                createdAt = post.createdAt,
                visibility = post.visibility,
                url = post.url,
                sensitive = post.sensitive
            )
        }
    }
}
