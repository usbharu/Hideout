package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import java.time.Instant

@Suppress("LongParameterList")
interface IPostApiService {
    suspend fun createPost(postForm: dev.usbharu.hideout.domain.model.hideout.form.Post, userId: Long): Post
    suspend fun getById(id: Long, userId: Long?): Post
    suspend fun getAll(
        since: Instant? = null,
        until: Instant? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = null,
        userId: Long? = null
    ): List<Post>

    suspend fun getByUser(
        nameOrId: String,
        since: Instant? = null,
        until: Instant? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = null,
        userId: Long? = null
    ): List<Post>
}
