package dev.usbharu.hideout.service.post

import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post

interface PostService {
    suspend fun createLocal(post: PostCreateDto): Post
}
