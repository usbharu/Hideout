package dev.usbharu.hideout.service.post

import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.repository.PostRepository
import dev.usbharu.hideout.repository.UserRepository
import dev.usbharu.hideout.service.ap.APNoteService
import org.koin.core.annotation.Single
import org.springframework.stereotype.Service
import java.time.Instant

@Service
@Single
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val apNoteService: APNoteService
) : PostService {
    override suspend fun createLocal(post: PostCreateDto): Post {
        val user = userRepository.findById(post.userId) ?: throw UserNotFoundException("${post.userId} was not found")
        val id = postRepository.generateId()
        val createPost = Post.of(
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
        apNoteService.createNote(createPost)
        return internalCreate(createPost)
    }

    private suspend fun internalCreate(post: Post): Post = postRepository.save(post)
}
