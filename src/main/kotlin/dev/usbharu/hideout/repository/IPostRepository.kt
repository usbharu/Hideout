package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import java.time.Instant

@Suppress("LongParameterList")
interface IPostRepository {
    suspend fun generateId(): Long
    suspend fun save(post: Post): Post
    suspend fun findOneById(id: Long, userId: Long? = null): Post?
    suspend fun findByUrl(url: String): Post?
    suspend fun delete(id: Long)
    suspend fun findAll(
        since: Instant?,
        until: Instant?,
        minId: Long?,
        maxId: Long?,
        limit: Int?,
        userId: Long?
    ): List<Post>

    suspend fun findByUserNameAndDomain(
        username: String,
        s: String,
        since: Instant?,
        until: Instant?,
        minId: Long?,
        maxId: Long?,
        limit: Int?,
        userId: Long?
    ): List<Post>

    suspend fun findByUserId(
        idOrNull: Long,
        since: Instant?,
        until: Instant?,
        minId: Long?,
        maxId: Long?,
        limit: Int?,
        userId: Long?
    ): List<Post>

    suspend fun findByApId(id: String): Post?
}
