package dev.usbharu.hideout.domain.model.hideout.entity

import org.springframework.data.annotation.Id

data class Timeline(
    @Id
    val id: Long,
    val userId: Long,
    val timelineId: Long,
    val postId: Long,
    val postUserId: Long,
    val createdAt: Long,
    val replyId: Long?,
    val repostId: Long?,
    val visibility: Visibility,
    val sensitive: Boolean,
    val isLocal: Boolean
)
