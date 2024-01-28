package dev.usbharu.hideout.mastodon.service.notification

import dev.usbharu.hideout.domain.mastodon.model.generated.Notification
import dev.usbharu.hideout.mastodon.domain.model.NotificationType

interface NotificationApiService {
    @Suppress("LongParameterList")
    suspend fun notifications(
        loginUser: Long,
        maxId: Long?,
        minId: Long?,
        sinceId: Long?,
        limit: Int,
        types: List<NotificationType>,
        excludeTypes: List<NotificationType>,
        accountId: List<Long>
    ): List<Notification>

    suspend fun fingById(loginUser: Long, notificationId: Long): Notification?

    suspend fun clearAll(loginUser: Long)

    suspend fun dismiss(loginUser: Long, notificationId: Long)
}
