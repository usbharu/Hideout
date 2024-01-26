package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.core.domain.model.relationship.Relationship

interface NotificationManagimentService {
    fun sendNotification(relationship: Relationship, notificationRequest: NotificationRequest): Boolean
}
