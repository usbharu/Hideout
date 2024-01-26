package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.core.domain.model.relationship.Relationship

interface RelationshipNotificationManagementService {
    fun sendNotification(relationship: Relationship, notificationRequest: NotificationRequest): Boolean
}
