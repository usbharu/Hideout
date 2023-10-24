package dev.usbharu.hideout.service.post

import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.query.PostQueryService
import dev.usbharu.hideout.repository.PostRepository
import dev.usbharu.hideout.repository.UserRepository
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val timelineService: TimelineService,
    private val postQueryService: PostQueryService,
    private val postBuilder: Post.PostBuilder
) : PostService {
    private val interceptors = Collections.synchronizedList(mutableListOf<PostCreateInterceptor>())

    override suspend fun createLocal(post: PostCreateDto): Post {
        logger.info("START Create Local Post user: {}, media: {}", post.userId, post.mediaIds.size)
        val create = internalCreate(post, true)
        interceptors.forEach { it.run(create) }
        logger.info("SUCCESS Create Local Post url: {}", create.url)
        return create
    }

    override suspend fun createRemote(post: Post): Post {
        logger.info("START Create Remote Post user: {}, remote url: {}", post.userId, post.apId)
        val createdPost = internalCreate(post, false)
        logger.info("SUCCESS Create Remote Post url: {}", createdPost.url)
        return createdPost
    }

    override fun addInterceptor(postCreateInterceptor: PostCreateInterceptor) {
        interceptors.add(postCreateInterceptor)
    }

    private suspend fun internalCreate(post: Post, isLocal: Boolean): Post {
        val save = try {
            postRepository.save(post)
        } catch (_: ExposedSQLException) {
            postQueryService.findByApId(post.apId)
        }
        timelineService.publishTimeline(save, isLocal)
        return save
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
            mediaIds = post.mediaIds
        )
        return internalCreate(createPost, isLocal)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PostServiceImpl::class.java)
    }
}
