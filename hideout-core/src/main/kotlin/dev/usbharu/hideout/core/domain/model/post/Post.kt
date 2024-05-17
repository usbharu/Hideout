/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.domain.model.post

import dev.usbharu.hideout.application.config.CharacterLimit
import dev.usbharu.hideout.core.service.post.PostContentFormatter
import jakarta.validation.Validator
import jakarta.validation.constraints.Positive
import org.hibernate.validator.constraints.URL
import org.springframework.stereotype.Component
import java.time.Instant

data class Post private constructor(
    @get:Positive
    val id: Long,
    @get:Positive
    val actorId: Long,
    val overview: String? = null,
    val content: String,
    val text: String,
    @get:Positive
    val createdAt: Long,
    val visibility: Visibility,
    @get:URL
    val url: String,
    val repostId: Long? = null,
    val replyId: Long? = null,
    val sensitive: Boolean = false,
    @get:URL
    val apId: String = url,
    val mediaIds: List<Long> = emptyList(),
    val deleted: Boolean = false,
    val emojiIds: List<Long> = emptyList(),
) {

    @Component
    class PostBuilder(
        private val characterLimit: CharacterLimit,
        private val postContentFormatter: PostContentFormatter,
        private val validator: Validator,
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
            emojiIds: List<Long> = emptyList(),
            deleted: Boolean = false,
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

            val post = Post(
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
                deleted = deleted,
                emojiIds = emojiIds
            )

            val validate = validator.validate(post)

            for (constraintViolation in validate) {
                throw IllegalArgumentException("${constraintViolation.propertyPath} : ${constraintViolation.message}")
            }

            if (post.deleted) {
                return post.delete()
            }

            return post
        }

        @Suppress("LongParameterList")
        fun pureRepostOf(
            id: Long,
            actorId: Long,
            visibility: Visibility,
            createdAt: Instant,
            url: String,
            repost: Post,
            apId: String,
        ): Post {
            // リポストの公開範囲は元のポストより広くてはいけない
            val fixedVisibility = if (visibility.ordinal <= repost.visibility.ordinal) {
                repost.visibility
            } else {
                visibility
            }

            val post = of(
                id = id,
                actorId = actorId,
                overview = null,
                content = "",
                createdAt = createdAt.toEpochMilli(),
                visibility = fixedVisibility,
                url = url,
                repostId = repost.id,
                replyId = null,
                sensitive = false,
                apId = apId,
                mediaIds = emptyList(),
                deleted = false,
                emojiIds = emptyList()
            )
            return post
        }

        @Suppress("LongParameterList")
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
            emojiIds: List<Long> = emptyList(),
        ): Post {
            // リポストの公開範囲は元のポストより広くてはいけない
            val fixedVisibility = if (visibility.ordinal <= repost.visibility.ordinal) {
                repost.visibility
            } else {
                visibility
            }

            val post = of(
                id = id,
                actorId = actorId,
                overview = overview,
                content = content,
                createdAt = createdAt.toEpochMilli(),
                visibility = fixedVisibility,
                url = url,
                repostId = repost.id,
                replyId = replyId,
                sensitive = sensitive,
                apId = apId,
                mediaIds = mediaIds,
                deleted = false,
                emojiIds = emojiIds
            )
            return post
        }

    }

    fun isPureRepost(): Boolean =
        this.text.isEmpty() &&
                this.content.isEmpty() &&
                this.overview == null &&
                this.replyId == null &&
                this.repostId != null

    fun delete(): Post {
        return Post(
            id = this.id,
            actorId = actorId,
            overview = overview,
            content = content,
            text = text,
            createdAt = createdAt,
            visibility = visibility,
            url = url,
            repostId = repostId,
            replyId = replyId,
            sensitive = sensitive,
            apId = apId,
            mediaIds = mediaIds,
            deleted = true
        )
    }
}
