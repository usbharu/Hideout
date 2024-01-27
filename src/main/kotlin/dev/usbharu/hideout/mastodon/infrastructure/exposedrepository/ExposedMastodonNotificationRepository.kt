package dev.usbharu.hideout.mastodon.infrastructure.exposedrepository

import dev.usbharu.hideout.core.infrastructure.exposedrepository.AbstractRepository
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Timelines
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
            MastodonNotifications.select { MastodonNotifications.id eq mastodonNotification.id }.singleOrNull()
        if (singleOrNull == null) {
            MastodonNotifications.insert {
                it[MastodonNotifications.id] = mastodonNotification.id
                it[MastodonNotifications.type] = mastodonNotification.type.name
                it[MastodonNotifications.createdAt] = mastodonNotification.createdAt
                it[MastodonNotifications.accountId] = mastodonNotification.accountId
                it[MastodonNotifications.statusId] = mastodonNotification.statusId
                it[MastodonNotifications.reportId] = mastodonNotification.reportId
                it[MastodonNotifications.relationshipServeranceEventId] =
                    mastodonNotification.relationshipServeranceEvent
            }
        } else {
            MastodonNotifications.update({ MastodonNotifications.id eq mastodonNotification.id }) {
                it[MastodonNotifications.type] = mastodonNotification.type.name
                it[MastodonNotifications.createdAt] = mastodonNotification.createdAt
                it[MastodonNotifications.accountId] = mastodonNotification.accountId
                it[MastodonNotifications.statusId] = mastodonNotification.statusId
                it[MastodonNotifications.reportId] = mastodonNotification.reportId
                it[MastodonNotifications.relationshipServeranceEventId] =
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
        MastodonNotifications.select { MastodonNotifications.id eq id }.singleOrNull()?.toMastodonNotification()
    }

    override suspend fun findByUserIdAndMaxIdAndMinIdAndSinceIdAndInTypesAndInSourceActorId(
        loginUser: Long,
        maxId: Long?,
        minId: Long?,
        sinceId: Long?,
        limit: Int,
        typesTmp: MutableList<NotificationType>,
        accountId: List<Long>
    ): List<MastodonNotification> = query {
        val query = MastodonNotifications.select {
            MastodonNotifications.userId eq loginUser
        }


        if (maxId != null) {
            query.andWhere { MastodonNotifications.id lessEq maxId }
        }
        if (minId != null) {
            query.andWhere { MastodonNotifications.id greaterEq minId }
        }
        if (sinceId != null) {
            query.andWhere { MastodonNotifications.id greaterEq sinceId }
        }
        val result = query
            .limit(limit)
            .orderBy(Timelines.createdAt, SortOrder.DESC)

        return@query result.map { it.toMastodonNotification() }
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

fun ResultRow.toMastodonNotification(): MastodonNotification {
    return MastodonNotification(
        this[MastodonNotifications.id],
        this[MastodonNotifications.userId],
        NotificationType.valueOf(this[MastodonNotifications.type]),
        this[MastodonNotifications.createdAt],
        this[MastodonNotifications.accountId],
        this[MastodonNotifications.statusId],
        this[MastodonNotifications.reportId],
        this[MastodonNotifications.relationshipServeranceEventId],
    )
}

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
