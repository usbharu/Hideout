package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.dto.PostResponse

@Suppress("LongParameterList")
interface PostResponseQueryService {
    suspend fun findById(id: Long, userId: Long?): PostResponse
    suspend fun findAll(
        since: Long? = null,
        until: Long? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = null,
        userId: Long? = null
    ): List<PostResponse>

    suspend fun findByUserId(
        userId: Long,
        since: Long? = null,
        until: Long? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = null,
        userId2: Long? = null
    ): List<PostResponse>

    suspend fun findByUserNameAndUserDomain(
        name: String,
        domain: String,
        since: Long? = null,
        until: Long? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = null,
        userId: Long? = null
    ): List<PostResponse>
}
