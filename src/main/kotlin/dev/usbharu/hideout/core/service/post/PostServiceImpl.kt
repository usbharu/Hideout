package dev.usbharu.hideout.core.service.post

import dev.usbharu.hideout.activitypub.service.activity.create.ApSendCreateService
import dev.usbharu.hideout.core.domain.exception.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.user.UserRepository
import dev.usbharu.hideout.core.query.PostQueryService
import dev.usbharu.hideout.core.service.timeline.TimelineService
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val timelineService: TimelineService,
    private val postQueryService: PostQueryService,
    private val postBuilder: Post.PostBuilder,
    private val apSendCreateService: ApSendCreateService
) : PostService {

    override suspend fun createLocal(post: PostCreateDto): Post {
        logger.info("START Create Local Post user: {}, media: {}", post.userId, post.mediaIds.size)
        val create = internalCreate(post, true)
        apSendCreateService.createNote(create)
        logger.info("SUCCESS Create Local Post url: {}", create.url)
        return create
    }

    override suspend fun createRemote(post: Post): Post {
        logger.info("START Create Remote Post user: {}, remote url: {}", post.userId, post.apId)
        val createdPost = internalCreate(post, false)
        logger.info("SUCCESS Create Remote Post url: {}", createdPost.url)
        return createdPost
    }

    private suspend fun internalCreate(post: Post, isLocal: Boolean): Post {
        return try {
            if (postRepository.save(post)) {
                try {
                    timelineService.publishTimeline(post, isLocal)
                } catch (e: DuplicateKeyException) {
                    logger.trace("Timeline already exists.", e)
                }
            }
            post
        } catch (e: ExposedSQLException) {
            logger.warn("FAILED Save to post. url: ${post.apId}", e)
            postQueryService.findByApId(post.apId)
        }
    }

    private suspend fun internalCreate(post: PostCreateDto, isLocal: Boolean): Post {
        val user = userRepository.findById(post.userId) ?: throw UserNotFoundException("${post.userId} was not found")
        val id = postRepository.generateId()
        val createPost = postBuilder.of(
            id = id,
            userId = post.userId,
            overview = post.overview,
            text = post.text,
            createdAt = Instant.now().toEpochMilli(),
            visibility = post.visibility,
            url = "${user.url}/posts/$id",
            mediaIds = post.mediaIds,
            replyId = post.repolyId,
            repostId = post.repostId,
        )
        return internalCreate(createPost, isLocal)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PostServiceImpl::class.java)
    }
}
