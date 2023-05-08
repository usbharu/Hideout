package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.repository.IPostRepository
import dev.usbharu.hideout.repository.Posts
import dev.usbharu.hideout.repository.UsersFollowers
import dev.usbharu.hideout.repository.toPost
import dev.usbharu.hideout.service.IPostService
import dev.usbharu.hideout.service.activitypub.ActivityPubNoteService
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

    override suspend fun create(post: Post) {
        logger.debug("create post={}", post)
        val postEntity = postRepository.save(post)
        activityPubNoteService.createNote(postEntity)
    }

    override suspend fun create(post: PostCreateDto) {
        logger.debug("create post={}", post)
        val user = userService.findByNameLocalUser(post.username)
        val id = postRepository.generateId()
        val postEntity = Post(
            id, user.id, null, post.text,
            Instant.now().toEpochMilli(), 0, "${user.url}/posts/$id", null, null
        )
        postRepository.save(postEntity)
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
                Posts.visibility.eq(0)
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

    override suspend fun delete(id: String) {
        TODO("Not yet implemented")
    }
}
