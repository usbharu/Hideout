package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.core.domain.model.notification.Notification

interface NotificationService {
    suspend fun publishNotify(notificationRequest: NotificationRequest): Notification
    suspend fun unpublishNotify(notificationId: Long)
}
