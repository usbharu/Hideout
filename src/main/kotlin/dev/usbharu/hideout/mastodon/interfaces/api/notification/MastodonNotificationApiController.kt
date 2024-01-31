package dev.usbharu.hideout.mastodon.interfaces.api.notification

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.toHttpHeader
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
    private val notificationApiService: NotificationApiService,
    private val applicationConfig: ApplicationConfig
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
        val notifications = notificationApiService.notifications(
            loginUser = loginUserContextHolder.getLoginUserId(),
            types = types.orEmpty().mapNotNull { NotificationType.parse(it) },
            excludeTypes = excludeTypes.orEmpty().mapNotNull { NotificationType.parse(it) },
            accountId = accountId.orEmpty().mapNotNull { it.toLongOrNull() },
            page = Page.of(
                maxId?.toLongOrNull(),
                sinceId?.toLongOrNull(),
                minId?.toLongOrNull(),
                limit?.coerceIn(0, 80) ?: 40
            )
        )

        val httpHeader = notifications.toHttpHeader(
            { "${applicationConfig.url}/api/v1/notifications?min_id=$it" },
            { "${applicationConfig.url}/api/v1/notifications?max_id=$it" }
        ) ?: return@runBlocking ResponseEntity.ok(
            notifications.asFlow()
        )

        ResponseEntity.ok().header("Link", httpHeader).body(notifications.asFlow())
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
