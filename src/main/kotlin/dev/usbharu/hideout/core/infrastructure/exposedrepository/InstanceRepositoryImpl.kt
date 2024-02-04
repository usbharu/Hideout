package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.model.instance.InstanceRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import dev.usbharu.hideout.core.domain.model.instance.Instance as InstanceEntity

@Repository
class InstanceRepositoryImpl(private val idGenerateService: IdGenerateService) : InstanceRepository,
    AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(instance: InstanceEntity): InstanceEntity = query {
        if (Instance.selectAll().where { Instance.id.eq(instance.id) }.forUpdate().empty()) {
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
        return@query instance
    }

    override suspend fun findById(id: Long): InstanceEntity? = query {
        return@query Instance.selectAll().where { Instance.id eq id }
            .singleOrNull()?.toInstance()
    }

    override suspend fun delete(instance: InstanceEntity): Unit = query {
        Instance.deleteWhere { id eq instance.id }
    }

    override suspend fun findByUrl(url: String): dev.usbharu.hideout.core.domain.model.instance.Instance? = query {
        return@query Instance.selectAll().where { Instance.url eq url }.singleOrNull()?.toInstance()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(InstanceRepositoryImpl::class.java)
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
    val url = varchar("url", 255).uniqueIndex()
    val iconUrl = varchar("icon_url", 255)
    val sharedInbox = varchar("shared_inbox", 255).nullable().uniqueIndex()
    val software = varchar("software", 255)
    val version = varchar("version", 255)
    val isBlocked = bool("is_blocked")
    val isMuted = bool("is_muted")
    val moderationNote = varchar("moderation_note", 10000)
    val createdAt = timestamp("created_at")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
