package dev.usbharu.hideout.repository

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

    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun insert(post: Post): PostEntity {
        return query {

            val generateId = idGenerateService.generateId()
            Posts.insert {
                it[id] = generateId
                it[userId] = post.userId
                it[overview] = post.overview
                it[text] = post.text
                it[createdAt] = post.createdAt
                it[visibility] = post.visibility
                it[url] = post.url
                it[repostId] = post.repostId
                it[replyId] = post.replyId
            }
            return@query PostEntity(
                generateId,
                post.userId,
                post.overview,
                post.text,
                post.createdAt,
                post.visibility,
                post.url,
                post.repostId,
                post.replyId
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
