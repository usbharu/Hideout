package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.domain.model.hideout.dto.PostResponse
import java.time.Instant

@Suppress("LongParameterList")
interface IPostApiService {
    suspend fun createPost(postForm: dev.usbharu.hideout.domain.model.hideout.form.Post, userId: Long): PostResponse
    suspend fun getById(id: Long, userId: Long?): PostResponse
    suspend fun getAll(
        since: Instant? = null,
        until: Instant? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = null,
        userId: Long? = null
    ): List<PostResponse>

    suspend fun getByUser(
        nameOrId: String,
        since: Instant? = null,
        until: Instant? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = null,
        userId: Long? = null
    ): List<PostResponse>
}
