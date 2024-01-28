package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class RelationshipNotificationManagementServiceImplTest {
    @Test
    fun `sendNotification ミューとしていない場合送信する`() {
        val notification = RelationshipNotificationManagementServiceImpl().sendNotification(
            Relationship(
                1,
                2,
                false,
                false,
                false,
                false,
                false
            ), PostNotificationRequest(1, 2, 3)
        )

        assertTrue(notification)

    }
}
