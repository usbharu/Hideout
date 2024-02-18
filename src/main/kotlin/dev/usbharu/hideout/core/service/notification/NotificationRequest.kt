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
import java.time.Instant

sealed class NotificationRequest(open val userId: Long, open val sourceActorId: Long?, val type: String) {
    abstract fun buildNotification(id: Long, createdAt: Instant): Notification
}

interface PostId {
    val postId: Long
}

data class MentionNotificationRequest(
    override val userId: Long,
    override val sourceActorId: Long,
    override val postId: Long
) : NotificationRequest(
    userId,
    sourceActorId,
    "mention"
),
    PostId {
    override fun buildNotification(id: Long, createdAt: Instant): Notification = Notification(
        id = id,
        type = type,
        userId = userId,
        sourceActorId = sourceActorId,
        postId = postId,
        text = null,
        reactionId = null,
        createdAt = createdAt
    )
}

data class PostNotificationRequest(
    override val userId: Long,
    override val sourceActorId: Long,
    override val postId: Long

) : NotificationRequest(userId, sourceActorId, "post"), PostId {
    override fun buildNotification(id: Long, createdAt: Instant): Notification = Notification(
        id = id,
        type = type,
        userId = userId,
        sourceActorId = sourceActorId,
        postId = postId,
        text = null,
        reactionId = null,
        createdAt = createdAt
    )
}

data class RepostNotificationRequest(
    override val userId: Long,
    override val sourceActorId: Long,
    override val postId: Long
) : NotificationRequest(userId, sourceActorId, "repost"), PostId {
    override fun buildNotification(id: Long, createdAt: Instant): Notification = Notification(
        id = id,
        type = type,
        userId = userId,
        sourceActorId = sourceActorId,
        postId = postId,
        text = null,
        reactionId = null,
        createdAt = createdAt
    )
}

data class FollowNotificationRequest(
    override val userId: Long,
    override val sourceActorId: Long
) : NotificationRequest(userId, sourceActorId, "follow") {
    override fun buildNotification(id: Long, createdAt: Instant): Notification = Notification(
        id = id,
        type = type,
        userId = userId,
        sourceActorId = sourceActorId,
        postId = null,
        text = null,
        reactionId = null,
        createdAt = createdAt
    )
}

data class FollowRequestNotificationRequest(
    override val userId: Long,
    override val sourceActorId: Long
) : NotificationRequest(userId, sourceActorId, "follow-request") {
    override fun buildNotification(id: Long, createdAt: Instant): Notification = Notification(
        id = id,
        type = type,
        userId = userId,
        sourceActorId = sourceActorId,
        postId = null,
        text = null,
        reactionId = null,
        createdAt = createdAt
    )
}

data class ReactionNotificationRequest(
    override val userId: Long,
    override val sourceActorId: Long,
    override val postId: Long,
    val reactionId: Long

) : NotificationRequest(userId, sourceActorId, "reaction"), PostId {
    override fun buildNotification(id: Long, createdAt: Instant): Notification = Notification(
        id = id,
        type = type,
        userId = userId,
        sourceActorId = sourceActorId,
        postId = postId,
        text = null,
        reactionId = reactionId,
        createdAt = createdAt
    )
}
