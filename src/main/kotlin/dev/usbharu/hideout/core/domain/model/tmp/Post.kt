package dev.usbharu.hideout.core.domain.model.tmp

import java.time.Instant

data class Post(
    val id: Long,
    val userId: Long,
    val overview: String?,
    val text: String,
    val visibility: Visibility,
    val url: String,
    val repostId: Long,
    val replyId: Long,
    val sensitive: Boolean,
    val apId: String,
    val mediaIds: List<Media>,
    val tag: List<String>,
    val mention: List<Long>,
    val createdAt: Instant
)
