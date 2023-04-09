package dev.usbharu.hideout.domain.model

import org.jetbrains.exposed.dao.id.LongIdTable

data class User(
    val name: String,
    val domain: String,
    val screenName: String,
    val description: String,
    val inbox: String,
    val outbox: String,
    val url: String
)

data class UserEntity(
    val id: Long,
    val name: String,
    val domain: String,
    val screenName: String,
    val description: String,
    val inbox: String,
    val outbox: String,
    val url: String
) {
    constructor(id: Long, user: User) : this(
        id,
        user.name,
        user.domain,
        user.screenName,
        user.description,
        user.inbox,
        user.outbox,
        user.url
    )
}

object Users : LongIdTable("users") {
    val name = varchar("name", length = 64)
    val domain = varchar("domain", length = 255)
    val screenName = varchar("screen_name", length = 64)
    val description = varchar("description", length = 600)
    val inbox = varchar("inbox", length = 255).uniqueIndex()
    val outbox = varchar("outbox", length = 255).uniqueIndex()
    val url = varchar("url", length = 255).uniqueIndex()

    init {
        uniqueIndex(name, domain)
    }
}
