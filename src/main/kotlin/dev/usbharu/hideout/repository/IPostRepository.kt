package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Post

@Suppress("LongParameterList")
interface IPostRepository {
    suspend fun generateId(): Long
    suspend fun save(post: Post): Post
    suspend fun findOneById(id: Long, userId: Long? = null): Post?
    suspend fun findByUrl(url: String): Post?
    suspend fun delete(id: Long)

    suspend fun findByApId(id: String): Post?
}
