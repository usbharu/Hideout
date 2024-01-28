package dev.usbharu.hideout.mastodon.service.notification

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.notification.Notification
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.service.notification.NotificationStore
import dev.usbharu.hideout.mastodon.domain.model.MastodonNotification
import dev.usbharu.hideout.mastodon.domain.model.MastodonNotificationRepository
import dev.usbharu.hideout.mastodon.domain.model.NotificationType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MastodonNotificationStore(private val mastodonNotificationRepository: MastodonNotificationRepository) :
    NotificationStore {
    override suspend fun publishNotification(
        notification: Notification,
        user: Actor,
        sourceActor: Actor?,
        post: Post?,
        reaction: Reaction?
    ): Boolean {
        val notificationType = when (notification.type) {
            "mention" -> NotificationType.mention
            "post" -> NotificationType.status
            "repost" -> NotificationType.reblog
            "follow" -> NotificationType.follow
            "follow-request" -> NotificationType.follow_request
            "reaction" -> NotificationType.favourite
            else -> {
                logger.debug("Notification type does not support. type: {}", notification.type)
                return false
            }
        }

        if (notification.sourceActorId == null) {
            logger.debug("Notification does not support. notification.sourceActorId is null")
            return false
        }

        val mastodonNotification = MastodonNotification(
            id = notification.id,
            userId = notification.userId,
            type = notificationType,
            createdAt = notification.createdAt,
            accountId = notification.sourceActorId,
            statusId = notification.postId,
            reportId = null,
            relationshipServeranceEvent = null
        )

        mastodonNotificationRepository.save(mastodonNotification)

        return true
    }

    override suspend fun unpulishNotification(notificationId: Long): Boolean {
        mastodonNotificationRepository.deleteById(notificationId)
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MastodonNotificationStore::class.java)
    }
}
