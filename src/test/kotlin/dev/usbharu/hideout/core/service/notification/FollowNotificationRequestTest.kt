package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.core.domain.model.notification.Notification
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant

class FollowNotificationRequestTest {

    @Test
    fun buildNotification() {
        val createdAt = Instant.now()
        val actual = FollowNotificationRequest(1, 2).buildNotification(1, createdAt)

        Assertions.assertThat(actual).isEqualTo(
            Notification(
                id = 1,
                type = "follow",
                userId = 1,
                sourceActorId = 2,
                postId = null,
                text = null,
                reactionId = null,
                createdAt = createdAt
            )
        )
    }
}
