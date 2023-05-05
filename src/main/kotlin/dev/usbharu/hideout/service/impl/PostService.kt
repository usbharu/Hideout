package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.repository.IPostRepository
import dev.usbharu.hideout.service.IPostService
import dev.usbharu.hideout.service.activitypub.ActivityPubNoteService
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
}
