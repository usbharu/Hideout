package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.core.domain.model.notification.Notification
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant

class ReactionNotificationRequestTest {

    @Test
    fun buildNotification() {
        val createdAt = Instant.now()
        val actual = ReactionNotificationRequest(1, 2, 3, 4).buildNotification(1, createdAt)

        Assertions.assertThat(actual).isEqualTo(
            Notification(
                id = 1,
                type = "reaction",
                userId = 1,
                sourceActorId = 2,
                postId = 3,
                text = null,
                reactionId = 4,
                createdAt = createdAt
            )
        )
    }
}
