package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import org.springframework.stereotype.Service

@Service
class RelationshipNotificationManagementServiceImpl : RelationshipNotificationManagementService {
    override fun sendNotification(relationship: Relationship, notificationRequest: NotificationRequest): Boolean =
        relationship.muting.not()
}
