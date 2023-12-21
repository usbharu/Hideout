package dev.usbharu.hideout.core.domain.model.post

import dev.usbharu.hideout.application.config.CharacterLimit
import org.springframework.stereotype.Component
import java.time.Instant

data class Post private constructor(
    val id: Long,
    val actorId: Long,
    val overview: String? = null,
    val text: String,
    val createdAt: Long,
    val visibility: Visibility,
    val url: String,
    val repostId: Long? = null,
    val replyId: Long? = null,
    val sensitive: Boolean = false,
    val apId: String = url,
    val mediaIds: List<Long> = emptyList(),
    val delted: Boolean = false,
    val emojiIds: List<Long> = emptyList()
) {

    @Component
    class PostBuilder(private val characterLimit: CharacterLimit) {
        @Suppress("FunctionMinLength", "LongParameterList")
        fun of(
            id: Long,
            actorId: Long,
            overview: String? = null,
            text: String,
            createdAt: Long,
            visibility: Visibility,
            url: String,
            repostId: Long? = null,
            replyId: Long? = null,
            sensitive: Boolean = false,
            apId: String = url,
            mediaIds: List<Long> = emptyList(),
            emojiIds: List<Long> = emptyList()
        ): Post {
            require(id >= 0) { "id must be greater than or equal to 0." }

            require(actorId >= 0) { "actorId must be greater than or equal to 0." }

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
                actorId = actorId,
                overview = limitedOverview,
                text = limitedText,
                createdAt = createdAt,
                visibility = visibility,
                url = url,
                repostId = repostId,
                replyId = replyId,
                sensitive = sensitive,
                apId = apId,
                mediaIds = mediaIds,
                delted = false,
                emojiIds = emojiIds
            )
        }

        @Suppress("LongParameterList")
        fun deleteOf(
            id: Long,
            visibility: Visibility,
            url: String,
            repostId: Long?,
            replyId: Long?,
            apId: String
        ): Post {
            return Post(
                id = id,
                actorId = 0,
                overview = null,
                text = "",
                createdAt = Instant.EPOCH.toEpochMilli(),
                visibility = visibility,
                url = url,
                repostId = repostId,
                replyId = replyId,
                sensitive = false,
                apId = apId,
                mediaIds = emptyList(),
                delted = true
            )
        }
    }

    fun delete(): Post {
        return Post(
            id = this.id,
            actorId = 0,
            overview = null,
            text = "",
            createdAt = Instant.EPOCH.toEpochMilli(),
            visibility = visibility,
            url = url,
            repostId = repostId,
            replyId = replyId,
            sensitive = false,
            apId = apId,
            mediaIds = emptyList(),
            delted = true
        )
    }
}
