package dev.usbharu.hideout.mastodon.infrastructure.exposedrepository

import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.application.infrastructure.exposed.withPagination
import dev.usbharu.hideout.core.infrastructure.exposedrepository.AbstractRepository
import dev.usbharu.hideout.mastodon.domain.model.MastodonNotification
import dev.usbharu.hideout.mastodon.domain.model.MastodonNotificationRepository
import dev.usbharu.hideout.mastodon.domain.model.NotificationType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository

@Repository
@Qualifier("jdbc")
@ConditionalOnProperty("hideout.use-mongodb", havingValue = "false", matchIfMissing = true)
class ExposedMastodonNotificationRepository : MastodonNotificationRepository, AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun save(mastodonNotification: MastodonNotification): MastodonNotification = query {
        val singleOrNull =
            MastodonNotifications.selectAll().where { MastodonNotifications.id eq mastodonNotification.id }
                .singleOrNull()
        if (singleOrNull == null) {
            MastodonNotifications.insert {
                it[id] = mastodonNotification.id
                it[type] = mastodonNotification.type.name
                it[createdAt] = mastodonNotification.createdAt
                it[accountId] = mastodonNotification.accountId
                it[statusId] = mastodonNotification.statusId
                it[reportId] = mastodonNotification.reportId
                it[relationshipServeranceEventId] =
                    mastodonNotification.relationshipServeranceEvent
            }
        } else {
            MastodonNotifications.update({ MastodonNotifications.id eq mastodonNotification.id }) {
                it[type] = mastodonNotification.type.name
                it[createdAt] = mastodonNotification.createdAt
                it[accountId] = mastodonNotification.accountId
                it[statusId] = mastodonNotification.statusId
                it[reportId] = mastodonNotification.reportId
                it[relationshipServeranceEventId] =
                    mastodonNotification.relationshipServeranceEvent
            }
        }
        mastodonNotification
    }

    override suspend fun deleteById(id: Long): Unit = query {
        MastodonNotifications.deleteWhere {
            MastodonNotifications.id eq id
        }
    }

    override suspend fun findById(id: Long): MastodonNotification? = query {
        MastodonNotifications.selectAll().where { MastodonNotifications.id eq id }.singleOrNull()
            ?.toMastodonNotification()
    }

    override suspend fun findByUserIdAndInTypesAndInSourceActorId(
        loginUser: Long,
        types: List<NotificationType>,
        accountId: List<Long>,
        page: Page
    ): PaginationList<MastodonNotification, Long> = query {
        val query = MastodonNotifications.selectAll().where { MastodonNotifications.userId eq loginUser }
        val result = query.withPagination(page, MastodonNotifications.id)

        return@query PaginationList(result.map { it.toMastodonNotification() }, result.next, result.prev)
    }

    override suspend fun deleteByUserId(userId: Long) {
        MastodonNotifications.deleteWhere {
            MastodonNotifications.userId eq userId
        }
    }

    override suspend fun deleteByUserIdAndId(userId: Long, id: Long) {
        MastodonNotifications.deleteWhere {
            MastodonNotifications.userId eq userId and (MastodonNotifications.id eq id)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedMastodonNotificationRepository::class.java)
    }
}

fun ResultRow.toMastodonNotification(): MastodonNotification = MastodonNotification(
    id = this[MastodonNotifications.id],
    userId = this[MastodonNotifications.userId],
    type = NotificationType.valueOf(this[MastodonNotifications.type]),
    createdAt = this[MastodonNotifications.createdAt],
    accountId = this[MastodonNotifications.accountId],
    statusId = this[MastodonNotifications.statusId],
    reportId = this[MastodonNotifications.reportId],
    relationshipServeranceEvent = this[MastodonNotifications.relationshipServeranceEventId],
)

object MastodonNotifications : Table("mastodon_notifications") {
    val id = long("id")
    val userId = long("user_id")
    val type = varchar("type", 100)
    val createdAt = timestamp("created_at")
    val accountId = long("account_id")
    val statusId = long("status_id").nullable()
    val reportId = long("report_id").nullable()
    val relationshipServeranceEventId = long("relationship_serverance_event_id").nullable()
}
