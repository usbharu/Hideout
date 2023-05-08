package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.service.IdGenerateService
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single

@Single
class PostRepositoryImpl(database: Database, private val idGenerateService: IdGenerateService) : IPostRepository {

    init {
        transaction(database) {
            SchemaUtils.create(Posts)
        }
    }

    override suspend fun generateId(): Long = idGenerateService.generateId()

    @Suppress("InjectDispatcher")
    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }


    override suspend fun save(post: Post): Post {
        return query {
            Posts.insert {
                it[id] = post.id
                it[userId] = post.userId
                it[overview] = post.overview
                it[text] = post.text
                it[createdAt] = post.createdAt
                it[visibility] = post.visibility
                it[url] = post.url
                it[repostId] = post.repostId
                it[replyId] = post.replyId
            }
            return@query post
        }
    }

    override suspend fun findOneById(id: Long): Post {
        return query {
            Posts.select { Posts.id eq id }.single().toPost()
        }
    }

    override suspend fun delete(id: Long) {
        return query {
            Posts.deleteWhere { Posts.id eq id }
        }
    }
}

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

fun ResultRow.toPost(): Post {
    return Post(
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
