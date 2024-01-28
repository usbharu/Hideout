package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.core.domain.model.notification.Notification
import java.time.Instant

sealed class NotificationRequest(open val userId: Long, open val sourceActorId: Long?, val type: String) {
    abstract fun buildNotification(id: Long, createdAt: Instant): Notification
}

interface PostId {
    val postId: Long
}

data class MentionNotificationRequest(
    override val userId: Long,
    override val sourceActorId: Long,
    override val postId: Long
) : NotificationRequest(
    userId,
    sourceActorId,
    "mention"
),
    PostId {
    override fun buildNotification(id: Long, createdAt: Instant): Notification = Notification(
        id = id,
        type = type,
        userId = userId,
        sourceActorId = sourceActorId,
        postId = postId,
        text = null,
        reactionId = null,
        createdAt = createdAt
    )
}

data class PostNotificationRequest(
    override val userId: Long,
    override val sourceActorId: Long,
    override val postId: Long

) : NotificationRequest(userId, sourceActorId, "post"), PostId {
    override fun buildNotification(id: Long, createdAt: Instant): Notification = Notification(
        id = id,
        type = type,
        userId = userId,
        sourceActorId = sourceActorId,
        postId = postId,
        text = null,
        reactionId = null,
        createdAt = createdAt
    )
}

data class RepostNotificationRequest(
    override val userId: Long,
    override val sourceActorId: Long,
    override val postId: Long
) : NotificationRequest(userId, sourceActorId, "repost"), PostId {
    override fun buildNotification(id: Long, createdAt: Instant): Notification = Notification(
        id = id,
        type = type,
        userId = userId,
        sourceActorId = sourceActorId,
        postId = postId,
        text = null,
        reactionId = null,
        createdAt = createdAt
    )
}

data class FollowNotificationRequest(
    override val userId: Long,
    override val sourceActorId: Long
) : NotificationRequest(userId, sourceActorId, "follow") {
    override fun buildNotification(id: Long, createdAt: Instant): Notification = Notification(
        id = id,
        type = type,
        userId = userId,
        sourceActorId = sourceActorId,
        postId = null,
        text = null,
        reactionId = null,
        createdAt = createdAt
    )
}

data class FollowRequestNotificationRequest(
    override val userId: Long,
    override val sourceActorId: Long
) : NotificationRequest(userId, sourceActorId, "follow-request") {
    override fun buildNotification(id: Long, createdAt: Instant): Notification = Notification(
        id = id,
        type = type,
        userId = userId,
        sourceActorId = sourceActorId,
        postId = null,
        text = null,
        reactionId = null,
        createdAt = createdAt
    )
}

data class ReactionNotificationRequest(
    override val userId: Long,
    override val sourceActorId: Long,
    override val postId: Long,
    val reactionId: Long

) : NotificationRequest(userId, sourceActorId, "reaction"), PostId {
    override fun buildNotification(id: Long, createdAt: Instant): Notification = Notification(
        id = id,
        type = type,
        userId = userId,
        sourceActorId = sourceActorId,
        postId = postId,
        text = null,
        reactionId = reactionId,
        createdAt = createdAt
    )
}
