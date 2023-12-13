package dev.usbharu.hideout.core.service.post

import dev.usbharu.hideout.activitypub.service.activity.create.ApSendCreateService
import dev.usbharu.hideout.core.domain.exception.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
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
    private val actorRepository: ActorRepository,
    private val timelineService: TimelineService,
    private val postQueryService: PostQueryService,
    private val postBuilder: Post.PostBuilder,
    private val apSendCreateService: ApSendCreateService,
    private val reactionRepository: ReactionRepository
) : PostService {

    override suspend fun createLocal(post: PostCreateDto): Post {
        logger.info("START Create Local Post user: {}, media: {}", post.userId, post.mediaIds.size)
        val create = internalCreate(post, true)
        apSendCreateService.createNote(create)
        logger.info("SUCCESS Create Local Post url: {}", create.url)
        return create
    }

    override suspend fun createRemote(post: Post): Post {
        logger.info("START Create Remote Post user: {}, remote url: {}", post.actorId, post.apId)
        val actor =
            actorRepository.findById(post.actorId) ?: throw UserNotFoundException("${post.actorId} was not found.")
        val createdPost = internalCreate(post, false, actor)
        logger.info("SUCCESS Create Remote Post url: {}", createdPost.url)
        return createdPost
    }

    override suspend fun deleteLocal(post: Post) {
        if (post.delted) {
            return
        }
        reactionRepository.deleteByPostId(post.id)
        postRepository.save(post.delete())
        val actor = actorRepository.findById(post.actorId)
            ?: throw IllegalStateException("actor: ${post.actorId} was not found.")

        actorRepository.save(actor.decrementPostsCount())
    }

    override suspend fun deleteRemote(post: Post) {
        if (post.delted) {
            return
        }
        reactionRepository.deleteByPostId(post.id)
        postRepository.save(post.delete())

        val actor = actorRepository.findById(post.actorId)
            ?: throw IllegalStateException("actor: ${post.actorId} was not found.")

        actorRepository.save(actor.decrementPostsCount())
    }

    override suspend fun deleteByActor(actorId: Long) {
        postQueryService.findByActorId(actorId).filterNot { it.delted }.forEach { postRepository.save(it.delete()) }

        val actor = actorRepository.findById(actorId)
            ?: throw IllegalStateException("actor: $actorId was not found.")

        actorRepository.save(actor.copy(postsCount = 0, lastPostDate = null))
    }

    private suspend fun internalCreate(post: Post, isLocal: Boolean, actor: Actor): Post {
        return try {
            if (postRepository.save(post)) {
                try {
                    timelineService.publishTimeline(post, isLocal)
                    actorRepository.save(actor.incrementPostsCount())
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
        val user = actorRepository.findById(post.userId) ?: throw UserNotFoundException("${post.userId} was not found")
        val id = postRepository.generateId()
        val createPost = postBuilder.of(
            id = id,
            actorId = post.userId,
            overview = post.overview,
            text = post.text,
            createdAt = Instant.now().toEpochMilli(),
            visibility = post.visibility,
            url = "${user.url}/posts/$id",
            mediaIds = post.mediaIds,
            replyId = post.repolyId,
            repostId = post.repostId,
        )
        return internalCreate(createPost, isLocal, user)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PostServiceImpl::class.java)
    }
}
