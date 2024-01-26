package dev.usbharu.hideout.core.domain.model.notification

import java.time.Instant

data class Notification(
    val id: Long,
    val userId: Long,
    val sourceActorId: Long?,
    val postId: Long?,
    val text: String?,
    val reactionId: Long?,
    val createdAt: Instant
)
