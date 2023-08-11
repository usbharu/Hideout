package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Post

@Suppress("LongParameterList")
interface PostRepository {
    suspend fun generateId(): Long
    suspend fun save(post: Post): Post
    suspend fun delete(id: Long)
    suspend fun findById(id: Long): Post
}
