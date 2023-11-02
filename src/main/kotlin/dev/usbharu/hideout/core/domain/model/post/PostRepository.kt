package dev.usbharu.hideout.core.domain.model.post

import org.springframework.stereotype.Repository

@Suppress("LongParameterList")
@Repository
interface PostRepository {
    suspend fun generateId(): Long
    suspend fun save(post: Post): Boolean
    suspend fun delete(id: Long)
    suspend fun findById(id: Long): Post
}
