package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmoji
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class CustomEmojiRepositoryImpl(private val idGenerateService: IdGenerateService) : CustomEmojiRepository,
    AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(customEmoji: CustomEmoji): CustomEmoji = query {
        val singleOrNull = CustomEmojis.select { CustomEmojis.id eq customEmoji.id }.forUpdate().singleOrNull()
        if (singleOrNull == null) {
            CustomEmojis.insert {
                it[CustomEmojis.id] = customEmoji.id
                it[CustomEmojis.name] = customEmoji.name
                it[CustomEmojis.domain] = customEmoji.domain
                it[CustomEmojis.instanceId] = customEmoji.instanceId
                it[CustomEmojis.url] = customEmoji.url
                it[CustomEmojis.category] = customEmoji.category
                it[CustomEmojis.createdAt] = customEmoji.createdAt
            }
        } else {
            CustomEmojis.update({ CustomEmojis.id eq customEmoji.id }) {
                it[CustomEmojis.name] = customEmoji.name
                it[CustomEmojis.domain] = customEmoji.domain
                it[CustomEmojis.instanceId] = customEmoji.instanceId
                it[CustomEmojis.url] = customEmoji.url
                it[CustomEmojis.category] = customEmoji.category
                it[CustomEmojis.createdAt] = customEmoji.createdAt
            }
        }
        return@query customEmoji
    }

    override suspend fun findById(id: Long): CustomEmoji? = query {
        return@query CustomEmojis.select { CustomEmojis.id eq id }.singleOrNull()?.toCustomEmoji()
    }

    override suspend fun delete(customEmoji: CustomEmoji): Unit = query {
        CustomEmojis.deleteWhere { CustomEmojis.id eq customEmoji.id }
    }

    override suspend fun findByNameAndDomain(name: String, domain: String): CustomEmoji? = query {
        return@query CustomEmojis
            .select { CustomEmojis.name eq name and (CustomEmojis.domain eq domain) }
            .singleOrNull()
            ?.toCustomEmoji()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CustomEmojiRepositoryImpl::class.java)
    }
}

fun ResultRow.toCustomEmoji(): CustomEmoji = CustomEmoji(
    id = this[CustomEmojis.id],
    name = this[CustomEmojis.name],
    domain = this[CustomEmojis.domain],
    instanceId = this[CustomEmojis.instanceId],
    url = this[CustomEmojis.url],
    category = this[CustomEmojis.category],
    createdAt = this[CustomEmojis.createdAt]
)

fun ResultRow.toCustomEmojiOrNull(): CustomEmoji? {
    return CustomEmoji(
        id = this.getOrNull(CustomEmojis.id) ?: return null,
        name = this.getOrNull(CustomEmojis.name) ?: return null,
        domain = this.getOrNull(CustomEmojis.domain) ?: return null,
        instanceId = this[CustomEmojis.instanceId],
        url = this.getOrNull(CustomEmojis.url) ?: return null,
        category = this[CustomEmojis.category],
        createdAt = this.getOrNull(CustomEmojis.createdAt) ?: return null
    )
}

object CustomEmojis : Table("emojis") {
    val id = long("id")
    val name = varchar("name", 1000)
    val domain = varchar("domain", 1000)
    val instanceId = long("instance_id").references(Instance.id).nullable()
    val url = varchar("url", 255).uniqueIndex()
    val category = varchar("category", 255).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())

    override val primaryKey: PrimaryKey = PrimaryKey(id)

    init {
        uniqueIndex(name, instanceId)
    }
}
