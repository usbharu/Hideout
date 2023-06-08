package dev.usbharu.hideout.service.post

import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.repository.IPostRepository
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.activitypub.ActivityPubNoteService
import org.koin.core.annotation.Single
import java.time.Instant

@Single
class PostServiceImpl(
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository,
    private val activityPubNoteService: ActivityPubNoteService
) : IPostService {
    override suspend fun createLocal(post: PostCreateDto): Post {
        val user = userRepository.findById(post.userId) ?: throw UserNotFoundException("${post.userId} was not found")
        val id = postRepository.generateId()
        val createPost = Post(
            id = id,
            userId = post.userId,
            overview = post.overview,
            text = post.text,
            createdAt = Instant.now().toEpochMilli(),
            visibility = post.visibility,
            url = "${user.url}/posts/$id",
            repostId = null,
            replyId = null
        )
        activityPubNoteService.createNote(createPost)
        return internalCreate(createPost)
    }

    private suspend fun internalCreate(post: Post): Post = postRepository.save(post)
}
