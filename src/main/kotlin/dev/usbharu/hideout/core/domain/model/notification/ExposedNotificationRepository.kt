package dev.usbharu.hideout.core.domain.model.notification

import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.infrastructure.exposedrepository.AbstractRepository
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Reactions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedNotificationRepository(private val idGenerateService: IdGenerateService) : NotificationRepository,
    AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(notification: Notification): Notification = query {
        val singleOrNull = Notifications.select {
            Notifications.id eq notification.id
        }.forUpdate().singleOrNull()
        if (singleOrNull == null) {
            Notifications.insert {
                it[id] = notification.id
                it[type] = notification.type
                it[userId] = notification.userId
                it[sourceActorId] = notification.sourceActorId
                it[postId] = notification.postId
                it[text] = notification.text
                it[reactionId] = notification.reactionId
                it[createdAt] = notification.createdAt
            }
        } else {
            Notifications.update({ Notifications.id eq notification.id }) {
                it[type] = notification.type
                it[userId] = notification.userId
                it[sourceActorId] = notification.sourceActorId
                it[postId] = notification.postId
                it[text] = notification.text
                it[reactionId] = notification.reactionId
                it[createdAt] = notification.createdAt
            }
        }
        notification
    }

    override suspend fun findById(id: Long): Notification? = query {
        Notifications.select { Notifications.id eq id }.singleOrNull()?.toNotifications()
    }

    override suspend fun deleteById(id: Long) {
        Notifications.deleteWhere { Notifications.id eq id }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedNotificationRepository::class.java)
    }
}

fun ResultRow.toNotifications() = Notification(
    id = this[Notifications.id],
    type = this[Notifications.type],
    userId = this[Notifications.userId],
    sourceActorId = this[Notifications.sourceActorId],
    postId = this[Notifications.postId],
    text = this[Notifications.text],
    reactionId = this[Notifications.reactionId],
    createdAt = this[Notifications.createdAt],
)

object Notifications : Table("notifications") {
    val id = long("id")
    val type = varchar("type", 100)
    val userId = long("user_id").references(Actors.id)
    val sourceActorId = long("source_actor_id").references(Actors.id).nullable()
    val postId = long("post_id").references(Posts.id).nullable()
    val text = varchar("text", 3000).nullable()
    val reactionId = long("reaction_id").references(Reactions.id).nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
