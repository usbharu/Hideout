package dev.usbharu.hideout.service

import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import java.time.Instant

interface IPostService {
    suspend fun create(post: Post)
    suspend fun create(post: PostCreateDto)
    suspend fun findAll(
        since: Instant? = null,
        until: Instant? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = 10,
        userId: Long? = null
    ): List<Post>

    suspend fun findById(id: String): Post
    suspend fun delete(id: String)
}
