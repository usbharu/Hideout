package dev.usbharu.hideout.mastodon.service.notification

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.domain.mastodon.model.generated.Notification
import dev.usbharu.hideout.mastodon.domain.model.MastodonNotificationRepository
import dev.usbharu.hideout.mastodon.domain.model.NotificationType
import dev.usbharu.hideout.mastodon.domain.model.NotificationType.*
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import dev.usbharu.hideout.mastodon.service.account.AccountService
import org.springframework.stereotype.Service

@Service
class NotificationApiServiceImpl(
    private val mastodonNotificationRepository: MastodonNotificationRepository,
    private val transaction: Transaction,
    private val accountService: AccountService,
    private val statusQueryService: StatusQueryService
) :
    NotificationApiService {
    override suspend fun notifications(
        loginUser: Long,
        maxId: Long?,
        minId: Long?,
        sinceId: Long?,
        limit: Int,
        types: List<NotificationType>,
        excludeTypes: List<NotificationType>,
        accountId: List<Long>
    ): List<Notification> = transaction.transaction {
        val typesTmp = mutableListOf<NotificationType>()

        typesTmp.addAll(types)
        typesTmp.removeAll(excludeTypes)

        val mastodonNotifications =
            mastodonNotificationRepository.findByUserIdAndMaxIdAndMinIdAndSinceIdAndInTypesAndInSourceActorId(
                loginUser,
                maxId,
                minId,
                sinceId,
                limit,
                typesTmp,
                accountId
            )

        val accounts = accountService.findByIds(
            mastodonNotifications.map {
                it.accountId
            }
        ).associateBy { it.id.toLong() }

        val statuses = statusQueryService.findByPostIds(mastodonNotifications.mapNotNull { it.statusId })
            .associateBy { it.id.toLong() }

        mastodonNotifications.map {
            Notification(
                id = it.id.toString(),
                type = convertNotificationType(it.type),
                createdAt = it.createdAt.toString(),
                account = accounts.getValue(it.accountId),
                status = statuses[it.statusId],
                report = null,
                relationshipSeveranceEvent = null
            )
        }
    }

    override suspend fun notifications(
        loginUser: Long,
        types: List<NotificationType>,
        excludeTypes: List<NotificationType>,
        accountId: List<Long>,
        page: Page
    ): PaginationList<Notification, Long> = transaction.transaction {
        val typesTmp = mutableListOf<NotificationType>()

        typesTmp.addAll(types)
        typesTmp.removeAll(excludeTypes)

        val mastodonNotifications =
            mastodonNotificationRepository.findByUserIdAndInTypesAndInSourceActorId(
                loginUser,
                typesTmp,
                accountId,
                page
            )

        val accounts = accountService.findByIds(
            mastodonNotifications.map {
                it.accountId
            }
        ).associateBy { it.id.toLong() }

        val statuses = statusQueryService.findByPostIds(mastodonNotifications.mapNotNull { it.statusId })
            .associateBy { it.id.toLong() }

        val notifications = mastodonNotifications.map {
            Notification(
                id = it.id.toString(),
                type = convertNotificationType(it.type),
                createdAt = it.createdAt.toString(),
                account = accounts.getValue(it.accountId),
                status = statuses[it.statusId],
                report = null,
                relationshipSeveranceEvent = null
            )
        }

        return@transaction PaginationList(notifications, mastodonNotifications.next, mastodonNotifications.prev)
    }

    override suspend fun fingById(loginUser: Long, notificationId: Long): Notification? {
        val findById = mastodonNotificationRepository.findById(notificationId) ?: return null

        if (findById.userId != loginUser) {
            return null
        }

        val account = accountService.findById(findById.accountId)
        val status = findById.statusId?.let { statusQueryService.findByPostId(it) }

        return Notification(
            id = findById.id.toString(),
            type = convertNotificationType(findById.type),
            createdAt = findById.createdAt.toString(),
            account = account,
            status = status,
            report = null,
            relationshipSeveranceEvent = null
        )
    }

    override suspend fun clearAll(loginUser: Long) {
        mastodonNotificationRepository.deleteByUserId(loginUser)
    }

    override suspend fun dismiss(loginUser: Long, notificationId: Long) {
        mastodonNotificationRepository.deleteByUserIdAndId(loginUser, notificationId)
    }

    private fun convertNotificationType(notificationType: NotificationType): Notification.Type =
        when (notificationType) {
            mention -> Notification.Type.mention
            status -> Notification.Type.status
            reblog -> Notification.Type.reblog
            follow -> Notification.Type.follow
            follow_request -> Notification.Type.follow
            favourite -> Notification.Type.followRequest
            poll -> Notification.Type.poll
            update -> Notification.Type.update
            admin_sign_up -> Notification.Type.adminPeriodSignUp
            admin_report -> Notification.Type.adminPeriodReport
            severed_relationships -> Notification.Type.severedRelationships
        }
}
