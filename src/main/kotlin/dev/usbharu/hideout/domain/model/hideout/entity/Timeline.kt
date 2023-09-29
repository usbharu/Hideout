package dev.usbharu.hideout.domain.model.hideout.entity

import org.springframework.data.annotation.Id
import java.time.Instant

data class Timeline(
    @Id
    val id: Long,
    val userId: Long,
    val timelineId: Long,
    val postId: Long,
    val postUserId: Long,
    val createdAt: Instant,
    val replyId: Long,
    val repostId: Long,
    val visibility: Visibility,
    val sensitive: Boolean
)
