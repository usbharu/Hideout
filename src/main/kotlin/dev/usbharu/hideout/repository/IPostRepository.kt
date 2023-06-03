package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Post

interface IPostRepository {
    suspend fun generateId(): Long
    suspend fun save(post: Post): Post
    suspend fun findOneById(id: Long): Post?
    suspend fun findByUrl(url: String): Post?
    suspend fun delete(id: Long)
}
