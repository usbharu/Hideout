package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.instance.InstanceRepository
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.springframework.stereotype.Repository
import dev.usbharu.hideout.core.domain.model.instance.Instance as InstanceEntity

@Repository
class InstanceRepositoryImpl : InstanceRepository {
    override suspend fun save(instance: InstanceEntity): InstanceEntity {
        if (Instance.select { Instance.id.eq(instance.id) }.firstOrNull() == null) {
            Instance.insert {
                it[id] = instance.id
                it[name] = instance.name
                it[description] = instance.description
                it[url] = instance.url
                it[iconUrl] = instance.iconUrl
                it[sharedInbox] = instance.sharedInbox
                it[software] = instance.software
                it[version] = instance.version
                it[isBlocked] = instance.isBlocked
                it[isMuted] = instance.isMuted
                it[moderationNote] = instance.moderationNote
                it[createdAt] = instance.createdAt
            }
        } else {
            Instance.update({ Instance.id eq instance.id }) {
                it[name] = instance.name
                it[description] = instance.description
                it[url] = instance.url
                it[iconUrl] = instance.iconUrl
                it[sharedInbox] = instance.sharedInbox
                it[software] = instance.software
                it[version] = instance.version
                it[isBlocked] = instance.isBlocked
                it[isMuted] = instance.isMuted
                it[moderationNote] = instance.moderationNote
                it[createdAt] = instance.createdAt
            }
        }
        return instance
    }

    override suspend fun findById(id: Long): InstanceEntity {
        return Instance.select { Instance.id eq id }
            .singleOr { FailedToGetResourcesException("id: $id doesn't exist.") }.toInstance()
    }

    override suspend fun delete(instance: InstanceEntity) {
        Instance.deleteWhere { Instance.id eq instance.id }
    }
}

fun ResultRow.toInstance(): InstanceEntity {
    return InstanceEntity(
        id = this[Instance.id],
        name = this[Instance.name],
        description = this[Instance.description],
        url = this[Instance.url],
        iconUrl = this[Instance.iconUrl],
        sharedInbox = this[Instance.sharedInbox],
        software = this[Instance.software],
        version = this[Instance.version],
        isBlocked = this[Instance.isBlocked],
        isMuted = this[Instance.isMuted],
        moderationNote = this[Instance.moderationNote],
        createdAt = this[Instance.createdAt]
    )
}

object Instance : Table("instance") {
    val id = long("id")
    val name = varchar("name", 1000)
    val description = varchar("description", 5000)
    val url = varchar("url", 255)
    val iconUrl = varchar("icon_url", 255)
    val sharedInbox = varchar("shared_inbox", 255)
    val software = varchar("software", 255)
    val version = varchar("version", 255)
    val isBlocked = bool("is_blocked")
    val isMuted = bool("is_muted")
    val moderationNote = varchar("moderation_note", 10000)
    val createdAt = timestamp("created_at")
}
