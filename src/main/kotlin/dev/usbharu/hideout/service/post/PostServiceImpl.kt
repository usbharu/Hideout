package dev.usbharu.hideout.service.post

import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.repository.PostRepository
import dev.usbharu.hideout.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val timelineService: TimelineService
) : PostService {
    private val interceptors = Collections.synchronizedList(mutableListOf<PostCreateInterceptor>())

    override suspend fun createLocal(post: PostCreateDto): Post {
        val create = internalCreate(post)
        interceptors.forEach { it.run(create) }
        return create
    }

    override suspend fun createRemote(post: Post): Post {
        return internalCreate(post)
    }

    override fun addInterceptor(postCreateInterceptor: PostCreateInterceptor) {
        interceptors.add(postCreateInterceptor)
    }

    private suspend fun internalCreate(post: Post): Post {
        timelineService.publishTimeline(post)
        return postRepository.save(post)
    }

    private suspend fun internalCreate(post: PostCreateDto): Post {
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
        return internalCreate(createPost)
    }
}
