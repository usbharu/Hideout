package dev.usbharu.hideout.service

import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post

interface IPostService {
    suspend fun create(post: Post)
    suspend fun create(post: PostCreateDto)
}
