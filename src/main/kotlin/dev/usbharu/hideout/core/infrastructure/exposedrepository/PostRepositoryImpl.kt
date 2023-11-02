package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository

@Repository
class PostRepositoryImpl(
    private val idGenerateService: IdGenerateService,
    private val postQueryMapper: QueryMapper<Post>
) : PostRepository {

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
            PostsMedia.batchInsert(post.mediaIds) {
                this[PostsMedia.postId] = post.id
                this[PostsMedia.mediaId] = it
            }
        } else {
            PostsMedia.deleteWhere {
                postId eq post.id
            }
            PostsMedia.batchInsert(post.mediaIds) {
                this[PostsMedia.postId] = post.id
                this[PostsMedia.mediaId] = it
            }
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

        assert(Posts.select { Posts.id eq post.id }.singleOrNull() != null) {
            "Faild to insert"
        }

        return post
    }

    override suspend fun findById(id: Long): Post =
        Posts.innerJoin(PostsMedia, onColumn = { Posts.id }, otherColumn = { postId })
            .select { Posts.id eq id }
            .let(postQueryMapper::map)
            .singleOrNull()
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

object PostsMedia : Table() {
    val postId = long("post_id").references(Posts.id, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val mediaId = long("media_id").references(Media.id, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(postId, mediaId)
}