package dev.usbharu.hideout.domain.model

import dev.usbharu.hideout.repository.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object Posts : Table() {
    val id = long("id")
    val userId = long("userId").references(Users.id)
    val overview = varchar("overview", 100).nullable()
    val text = varchar("text", 3000)
    val createdAt = long("createdAt")
    val visibility = integer("visibility").default(0)
    val url = varchar("url", 500)
    val repostId = long("repostId").references(id).nullable()
    val replyId = long("replyId").references(id).nullable()
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

data class Post(
    val userId: Long,
    val overview: String? = null,
    val text: String,
    val createdAt: Long,
    val visibility: Int,
    val repostId: Long? = null,
    val replyId: Long? = null
)

data class PostEntity(
    val id: Long,
    val userId: Long,
    val overview: String? = null,
    val text: String,
    val createdAt: Long,
    val visibility: Int,
    val url: String,
    val repostId: Long? = null,
    val replyId: Long? = null
)

fun ResultRow.toPost(): PostEntity {
    return PostEntity(
        id = this[Posts.id],
        userId = this[Posts.userId],
        overview = this[Posts.overview],
        text = this[Posts.text],
        createdAt = this[Posts.createdAt],
        visibility = this[Posts.visibility],
        url = this[Posts.url],
        repostId = this[Posts.repostId],
        replyId = this[Posts.replyId]
    )
}
