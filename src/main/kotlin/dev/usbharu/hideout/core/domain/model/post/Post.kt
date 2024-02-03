package dev.usbharu.hideout.core.domain.model.post

import dev.usbharu.hideout.application.config.CharacterLimit
import dev.usbharu.hideout.core.service.post.PostContentFormatter
import org.springframework.stereotype.Component
import java.time.Instant

data class Post private constructor(
    val id: Long,
    val actorId: Long,
    val overview: String? = null,
    val content: String,
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
    class PostBuilder(
        private val characterLimit: CharacterLimit,
        private val postContentFormatter: PostContentFormatter
    ) {
        @Suppress("FunctionMinLength", "LongParameterList")
        fun of(
            id: Long,
            actorId: Long,
            overview: String? = null,
            content: String,
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

            val limitedText = if (content.length >= characterLimit.post.text) {
                content.substring(0, characterLimit.post.text)
            } else {
                content
            }

            val (html, content1) = postContentFormatter.format(limitedText)

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
                content = html,
                text = content1,
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

        fun pureRepostOf(
            id: Long,
            actorId: Long,
            visibility: Visibility,
            createdAt: Instant,
            url: String,
            repost: Post,
            apId: String
        ): Post {
            // リポストの公開範囲は元のポストより広くてはいけない
            val fixedVisibility = if (visibility.ordinal <= repost.visibility.ordinal) {
                repost.visibility
            } else {
                visibility
            }

            require(id >= 0) { "id must be greater than or equal to 0." }

            require(actorId >= 0) { "actorId must be greater than or equal to 0." }

            return Post(
                id,
                actorId,
                null,
                "",
                "",
                createdAt.toEpochMilli(),
                fixedVisibility,
                url,
                repost.id,
                null,
                false,
                apId,
                emptyList(),
                false,
                emptyList()
            )
        }

        fun quoteRepostOf(
            id: Long,
            actorId: Long,
            overview: String? = null,
            content: String,
            createdAt: Instant,
            visibility: Visibility,
            url: String,
            repost: Post,
            replyId: Long? = null,
            sensitive: Boolean = false,
            apId: String = url,
            mediaIds: List<Long> = emptyList(),
            emojiIds: List<Long> = emptyList()
        ): Post {
            // リポストの公開範囲は元のポストより広くてはいけない
            val fixedVisibility = if (visibility.ordinal <= repost.visibility.ordinal) {
                repost.visibility
            } else {
                visibility
            }

            require(id >= 0) { "id must be greater than or equal to 0." }

            require(actorId >= 0) { "actorId must be greater than or equal to 0." }

            val limitedOverview = if ((overview?.length ?: 0) >= characterLimit.post.overview) {
                overview?.substring(0, characterLimit.post.overview)
            } else {
                overview
            }

            val limitedText = if (content.length >= characterLimit.post.text) {
                content.substring(0, characterLimit.post.text)
            } else {
                content
            }

            val (html, content1) = postContentFormatter.format(limitedText)

            require(url.isNotBlank()) { "url must contain non-blank characters" }
            require(url.length <= characterLimit.general.url) {
                "url must not exceed ${characterLimit.general.url} characters."
            }

            require((replyId ?: 0) >= 0) { "replyId must be greater then or equal to 0." }

            return Post(
                id = id,
                actorId = actorId,
                overview = limitedOverview,
                content = html,
                text = content1,
                createdAt = createdAt.toEpochMilli(),
                visibility = fixedVisibility,
                url = url,
                repostId = repost.id,
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
                content = "",
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

    fun isPureRepost(): Boolean =
        this.text.isEmpty() && this.content.isEmpty() && this.overview == null && this.replyId == null && this.repostId != null

    fun delete(): Post {
        return Post(
            id = this.id,
            actorId = 0,
            overview = null,
            content = "",
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
