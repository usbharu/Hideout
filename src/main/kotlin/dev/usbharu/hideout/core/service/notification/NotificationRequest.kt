package dev.usbharu.hideout.core.service.notification

sealed class NotificationRequest(open val userId: Long, open val sourceActorId: Long)

interface PostId {
    val postId: Long
}

data class MentionNotificationRequest(
    override val userId: Long, override val sourceActorId: Long, override val postId: Long
) : NotificationRequest(
    userId, sourceActorId
), PostId

data class PostNotificationRequest(
    override val userId: Long, override val sourceActorId: Long, override val postId: Long

) : NotificationRequest(userId, sourceActorId), PostId

data class RepostNotificationRequest(
    override val userId: Long, override val sourceActorId: Long, override val postId: Long
) : NotificationRequest(userId, sourceActorId), PostId

data class FollowNotificationRequest(
    override val userId: Long, override val sourceActorId: Long, override val postId: Long

) : NotificationRequest(userId, sourceActorId), PostId

data class FollowRequestNotificationRequest(
    override val userId: Long, override val sourceActorId: Long, override val postId: Long
) : NotificationRequest(userId, sourceActorId), PostId

data class ReactionNotificationRequest(
    override val userId: Long, override val sourceActorId: Long, override val postId: Long, val reactionId: Long

) : NotificationRequest(userId, sourceActorId), PostId
