package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.core.domain.model.notification.Notification
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class MentionNotificationRequestTest {

    @Test
    fun buildNotification() {
        val createdAt = Instant.now()
        val actual = MentionNotificationRequest(1, 2, 3).buildNotification(1, createdAt)

        assertThat(actual).isEqualTo(
            Notification(
                id = 1,
                type = "mention",
                userId = 1,
                sourceActorId = 2,
                postId = 3,
                text = null,
                reactionId = null,
                createdAt = createdAt
            )
        )
    }
}
