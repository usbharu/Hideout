package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.exception.FailedToGetResourcesException
import dev.usbharu.hideout.service.core.IdGenerateService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository

@Repository
class PostRepositoryImpl(private val idGenerateService: IdGenerateService) : PostRepository {

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(post: Post): Post {
        val singleOrNull = Posts.select { Posts.id eq post.id }.singleOrNull()
        if (singleOrNull == null) {
            Posts.insert {
                it[id] = post.id
                it[userId] = post.userId
                it[overview] = post.overview
                it[text] = post.text
                it[createdAt] = post.createdAt
                it[visibility] = post.visibility.ordinal
                it[url] = post.url
                it[repostId] = post.repostId
                it[replyId] = post.replyId
                it[sensitive] = post.sensitive
                it[apId] = post.apId
            }
        } else {
            Posts.update({ Posts.id eq post.id }) {
                it[userId] = post.userId
                it[overview] = post.overview
                it[text] = post.text
                it[createdAt] = post.createdAt
                it[visibility] = post.visibility.ordinal
                it[url] = post.url
                it[repostId] = post.repostId
                it[replyId] = post.replyId
                it[sensitive] = post.sensitive
                it[apId] = post.apId
            }
        }
        return post
    }

    override suspend fun findById(id: Long): Post = Posts.select { Posts.id eq id }.singleOrNull()?.toPost()
        ?: throw FailedToGetResourcesException("id: $id was not found.")

    override suspend fun delete(id: Long) {
        Posts.deleteWhere { Posts.id eq id }
    }
}

object Posts : Table() {
    val id: Column<Long> = long("id")
    val userId: Column<Long> = long("userId").references(Users.id)
    val overview: Column<String?> = varchar("overview", 100).nullable()
    val text: Column<String> = varchar("text", 3000)
    val createdAt: Column<Long> = long("createdAt")
    val visibility: Column<Int> = integer("visibility").default(0)
    val url: Column<String> = varchar("url", 500)
    val repostId: Column<Long?> = long("repostId").references(id).nullable()
    val replyId: Column<Long?> = long("replyId").references(id).nullable()
    val sensitive: Column<Boolean> = bool("sensitive").default(false)
    val apId: Column<String> = varchar("ap_id", 100).uniqueIndex()
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

fun ResultRow.toPost(): Post {
    return Post.of(
        id = this[Posts.id],
        userId = this[Posts.userId],
        overview = this[Posts.overview],
        text = this[Posts.text],
        createdAt = this[Posts.createdAt],
        visibility = Visibility.values().first { visibility -> visibility.ordinal == this[Posts.visibility] },
        url = this[Posts.url],
        repostId = this[Posts.repostId],
        replyId = this[Posts.replyId],
        sensitive = this[Posts.sensitive],
        apId = this[Posts.apId]
    )
}
