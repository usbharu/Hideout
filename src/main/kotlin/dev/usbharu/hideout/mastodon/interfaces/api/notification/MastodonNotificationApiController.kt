package dev.usbharu.hideout.mastodon.interfaces.api.notification

import dev.usbharu.hideout.controller.mastodon.generated.NotificationsApi
import dev.usbharu.hideout.core.infrastructure.springframework.security.LoginUserContextHolder
import dev.usbharu.hideout.domain.mastodon.model.generated.Notification
import dev.usbharu.hideout.mastodon.domain.model.NotificationType
import dev.usbharu.hideout.mastodon.service.notification.NotificationApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class MastodonNotificationApiController(
    private val loginUserContextHolder: LoginUserContextHolder,
    private val notificationApiService: NotificationApiService
) : NotificationsApi {
    override suspend fun apiV1NotificationsClearPost(): ResponseEntity<Any> {
        notificationApiService.clearAll(loginUserContextHolder.getLoginUserId())
        return ResponseEntity.ok(null)
    }

    override fun apiV1NotificationsGet(
        maxId: String?,
        sinceId: String?,
        minId: String?,
        limit: Int?,
        types: List<String>?,
        excludeTypes: List<String>?,
        accountId: List<String>?
    ): ResponseEntity<Flow<Notification>> = runBlocking {
        val notificationFlow = notificationApiService.notifications(
            loginUserContextHolder.getLoginUserId(),
            maxId?.toLong(),
            minId?.toLong(),
            sinceId?.toLong(),
            limit ?: 20,
            types.orEmpty().mapNotNull { NotificationType.parse(it) },
            excludeTypes = excludeTypes.orEmpty().mapNotNull { NotificationType.parse(it) },
            accountId = accountId.orEmpty().mapNotNull { it.toLongOrNull() }
        ).asFlow()
        ResponseEntity.ok(notificationFlow)
    }

    override suspend fun apiV1NotificationsIdDismissPost(id: String): ResponseEntity<Any> {
        notificationApiService.dismiss(loginUserContextHolder.getLoginUserId(), id.toLong())
        return ResponseEntity.ok(null)
    }

    override suspend fun apiV1NotificationsIdGet(id: String): ResponseEntity<Notification> {
        val notification = notificationApiService.fingById(loginUserContextHolder.getLoginUserId(), id.toLong())

        return ResponseEntity.ok(notification)
    }
}
