package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.dto.PostResponse

@Suppress("LongParameterList")
interface PostResponseQueryService {
    suspend fun findById(id: Long, userId: Long): PostResponse
    suspend fun findAll(
        since: Long,
        until: Long,
        minId: Long,
        maxId: Long,
        limit: Long,
        userId: Long
    ): List<PostResponse>

    suspend fun findByUserId(
        userId: Long,
        since: Long,
        until: Long,
        minId: Long,
        maxId: Long,
        limit: Long,
        userId2: Long
    ): List<PostResponse>
}
