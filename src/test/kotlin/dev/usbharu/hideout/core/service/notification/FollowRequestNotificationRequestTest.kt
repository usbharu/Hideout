/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.core.domain.model.notification.Notification
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant

class FollowRequestNotificationRequestTest {

    @Test
    fun buildNotification() {
        val createdAt = Instant.now()
        val actual = FollowRequestNotificationRequest(1, 2).buildNotification(1, createdAt)

        Assertions.assertThat(actual).isEqualTo(
            Notification(
                id = 1,
                type = "follow-request",
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
