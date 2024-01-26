package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.notification.Notification
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.reaction.Reaction

interface NotificationStore {
    suspend fun publishNotification(
        notification: Notification,
        user: Actor,
        sourceActor: Actor?,
        post: Post?,
        reaction: Reaction?
    ): Boolean

    suspend fun unpulishNotification(notificationId: Long): Boolean
}
