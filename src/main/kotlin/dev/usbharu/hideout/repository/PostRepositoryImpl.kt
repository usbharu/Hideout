package dev.usbharu.hideout.repository

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.Post
import dev.usbharu.hideout.domain.model.PostEntity
import dev.usbharu.hideout.domain.model.Posts
import dev.usbharu.hideout.domain.model.toPost
import dev.usbharu.hideout.service.IdGenerateService
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class PostRepositoryImpl(database: Database, private val idGenerateService: IdGenerateService) : IPostRepository {

    init {
        transaction(database) {
            SchemaUtils.create(Posts)
        }
    }

    @Suppress("InjectDispatcher")
    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun insert(post: Post): PostEntity {
        return query {
            val generateId = idGenerateService.generateId()
            val name = Users.select { Users.id eq post.userId }.single().toUser().name
            val postUrl = Config.configData.url + "/users/$name/posts/$generateId"
            Posts.insert {
                it[id] = generateId
                it[userId] = post.userId
                it[overview] = post.overview
                it[text] = post.text
                it[createdAt] = post.createdAt
                it[visibility] = post.visibility
                it[url] = postUrl
                it[repostId] = post.repostId
                it[replyId] = post.replyId
            }
            return@query PostEntity(
                id = generateId,
                userId = post.userId,
                overview = post.overview,
                text = post.text,
                createdAt = post.createdAt,
                visibility = post.visibility,
                url = postUrl,
                repostId = post.repostId,
                replyId = post.replyId
            )
        }
    }

    override suspend fun findOneById(id: Long): PostEntity {
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
