package dev.usbharu.hideout.domain.model

import org.h2.mvstore.type.LongDataType
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class User(
    val id:Long,
    val name: String,
    val domain: String,
    val screenName: String,
    val description: String,
    val password:String? = null,
    val inbox: String,
    val outbox: String,
    val url: String,
    val publicKey:String,
    val privateKey:String? = null,
    val createdAt:LocalDateTime
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

object Users : Table("users") {
    val id = long("id").uniqueIndex()
    val name = varchar("name", length = 64)
    val domain = varchar("domain", length = 255)
    val screenName = varchar("screen_name", length = 64)
    val description = varchar("description", length = 600)
    val password = varchar("password", length = 255).nullable()
    val inbox = varchar("inbox", length = 255).uniqueIndex()
    val outbox = varchar("outbox", length = 255).uniqueIndex()
    val url = varchar("url", length = 255).uniqueIndex()
    val publicKey = varchar("public_key", length = 10000)
    val privateKey = varchar("private_key", length = 10000)
    val createdAt = long("created_at")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
    init {
        uniqueIndex(name, domain)
    }
}


fun ResultRow.toUser(): User {
    return User(
        this[Users.id],
        this[Users.name],
        this[Users.domain],
        this[Users.screenName],
        this[Users.description],
        this[Users.password],
        this[Users.inbox],
        this[Users.outbox],
        this[Users.url],
        this[Users.publicKey],
        this[Users.privateKey],
        LocalDateTime.ofInstant(Instant.ofEpochMilli((this[Users.createdAt])), ZoneId.systemDefault())
    )
}
