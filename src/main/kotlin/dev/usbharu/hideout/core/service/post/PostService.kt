package dev.usbharu.hideout.core.service.post

import dev.usbharu.hideout.core.domain.model.post.Post
import org.springframework.stereotype.Service

@Service
interface PostService {
    suspend fun createLocal(post: PostCreateDto): Post
    suspend fun createRemote(post: Post): Post
}
