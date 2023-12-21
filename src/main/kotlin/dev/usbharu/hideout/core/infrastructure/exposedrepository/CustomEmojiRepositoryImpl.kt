package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.emoji.CustomEmoji
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CustomEmojiRepositoryImpl : CustomEmojiRepository, AbstractRepository() {
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

    override val logger: Logger
        get() = Companion.logger

    companion object {
        private val logger = LoggerFactory.getLogger(CustomEmojiRepositoryImpl::class.java)
    }
}

fun ResultRow.toCustomEmoji(): CustomEmoji = CustomEmoji(
    this[CustomEmojis.id],
    this[CustomEmojis.name],
    this[CustomEmojis.domain],
    this[CustomEmojis.instanceId],
    this[CustomEmojis.url],
    this[CustomEmojis.category],
    this[CustomEmojis.createdAt]
)

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
