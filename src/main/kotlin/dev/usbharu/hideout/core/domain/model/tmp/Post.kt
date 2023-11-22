package dev.usbharu.hideout.core.domain.model.tmp

import java.time.Instant

data class Post(
    val id: PostId,
    val userId: UserId,
    val overview: String?,
    val text: String,
    val visibility: Visibility,
    val url: String,
    val repostId: PostId?,
    val replyId: PostId?,
    val sensitive: Boolean,
    val apId: String,
    val mediaIds: List<Media>,
    val tag: List<String>,
    val mention: List<UserId>,
    val createdAt: Instant
)
