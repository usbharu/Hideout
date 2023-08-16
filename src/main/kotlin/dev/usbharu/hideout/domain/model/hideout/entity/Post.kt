package dev.usbharu.hideout.domain.model.hideout.entity

import dev.usbharu.hideout.config.Config

data class Post private constructor(
    val id: Long,
    val userId: Long,
    val overview: String? = null,
    val text: String,
    val createdAt: Long,
    val visibility: Visibility,
    val url: String,
    val repostId: Long? = null,
    val replyId: Long? = null,
    val sensitive: Boolean = false,
    val apId: String = url
) {
    companion object {
        fun of(
            id: Long,
            userId: Long,
            overview: String? = null,
            text: String,
            createdAt: Long,
            visibility: Visibility,
            url: String,
            repostId: Long? = null,
            replyId: Long? = null,
            sensitive: Boolean = false,
            apId: String = url
        ): Post {
            val characterLimit = Config.configData.characterLimit

            require(id >= 0) { "id must be greater than or equal to 0." }

            require(userId >= 0) { "userId must be greater than or equal to 0." }

            val limitedOverview = if ((overview?.length ?: 0) >= characterLimit.post.overview) {
                overview?.substring(0, characterLimit.post.overview)
            } else {
                overview
            }

            val limitedText = if (text.length >= characterLimit.post.text) {
                text.substring(0, characterLimit.post.text)
            } else {
                text
            }

            require(url.isNotBlank()) { "url must contain non-blank characters" }
            require(url.length <= characterLimit.general.url) {
                "url must not exceed ${characterLimit.general.url} characters."
            }

            require((repostId ?: 0) >= 0) { "repostId must be greater then or equal to 0." }
            require((replyId ?: 0) >= 0) { "replyId must be greater then or equal to 0." }

            return Post(
                id = id,
                userId = userId,
                overview = limitedOverview,
                text = limitedText,
                createdAt = createdAt,
                visibility = visibility,
                url = url,
                repostId = repostId,
                replyId = replyId,
                sensitive = sensitive,
                apId = apId
            )
        }
    }
}
