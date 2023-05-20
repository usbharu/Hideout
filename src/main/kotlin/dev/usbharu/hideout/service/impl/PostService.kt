package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.repository.IPostRepository
import dev.usbharu.hideout.repository.Posts
import dev.usbharu.hideout.repository.UsersFollowers
import dev.usbharu.hideout.repository.toPost
import dev.usbharu.hideout.service.IPostService
import dev.usbharu.hideout.service.activitypub.ActivityPubNoteService
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inSubQuery
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.orIfNotNull
import org.jetbrains.exposed.sql.orWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import java.time.Instant

@Single
class PostService(
    private val postRepository: IPostRepository,
    private val activityPubNoteService: ActivityPubNoteService,
    private val userService: IUserService
) : IPostService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun create(post: Post): Post {
        logger.debug("create post={}", post)
        val postEntity = postRepository.save(post)
        activityPubNoteService.createNote(postEntity)
        return post
    }

    override suspend fun create(post: PostCreateDto): Post {
        logger.debug("create post={}", post)
        val user = userService.findById(post.userId)
        val id = postRepository.generateId()
        val postEntity = Post(
            id = id,
            userId = user.id,
            overview = null,
            text = post.text,
            createdAt = Instant.now().toEpochMilli(),
            visibility = Visibility.PUBLIC,
            url = "${user.url}/posts/$id",
            repostId = null,
            replyId = null
        )
        postRepository.save(postEntity)
        return postEntity
    }

    override suspend fun findAll(
        since: Instant?,
        until: Instant?,
        minId: Long?,
        maxId: Long?,
        limit: Int?,
        userId: Long?
    ): List<Post> {
        return transaction {
            val select = Posts.select {
                Posts.visibility.eq(Visibility.PUBLIC.ordinal)
            }
            if (userId != null) {
                select.orWhere {
                    Posts.userId.inSubQuery(
                        UsersFollowers.slice(UsersFollowers.userId).select(UsersFollowers.followerId eq userId)
                    )
                }
            }
            select.map { it.toPost() }
        }
    }

    override suspend fun findById(id: String): Post {
        TODO("Not yet implemented")
    }

    override suspend fun findByIdForUser(id: Long, userId: Long?): Post? {
        return transaction {
            val select = Posts.select(
                Posts.id.eq(id).and(
                    Posts.visibility.eq(Visibility.PUBLIC.ordinal).orIfNotNull(
                        userId?.let {
                            Posts.userId.inSubQuery(
                                UsersFollowers.slice(UsersFollowers.userId).select(UsersFollowers.followerId.eq(userId))
                            )
                        }
                    )
                )
            )
            select.singleOrNull()?.toPost()
        }
    }

    override suspend fun findByUserIdForUser(
        userId: Long,
        since: Instant?,
        until: Instant?,
        minId: Long?,
        maxId: Long?,
        limit: Int?,
        forUserId: Long?
    ): List<Post> {
        TODO("Not yet implemented")
    }

    override suspend fun findByUserNameAndDomainForUser(
        userName: String,
        domain: String,
        since: Instant?,
        until: Instant?,
        minId: Long?,
        maxId: Long?,
        limit: Int?,
        forUserId: Long?
    ): List<Post> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: String) {
        TODO("Not yet implemented")
    }
}
