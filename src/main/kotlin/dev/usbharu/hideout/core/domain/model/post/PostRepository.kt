package dev.usbharu.hideout.core.domain.model.post

import org.springframework.stereotype.Repository

@Suppress("LongParameterList")
@Repository
interface PostRepository {
    suspend fun generateId(): Long
    suspend fun save(post: Post): Post
    suspend fun delete(id: Long)
    suspend fun findById(id: Long): Post?
    suspend fun findByUrl(url: String): Post?
    suspend fun findByUrl2(url: String): Post? {
        throw Exception()
    }

    suspend fun findByApId(apId: String): Post?
    suspend fun existByApIdWithLock(apId: String): Boolean
}
