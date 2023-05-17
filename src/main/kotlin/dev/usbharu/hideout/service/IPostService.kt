package dev.usbharu.hideout.service

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import java.time.Instant

interface IPostService {
    suspend fun create(post: Post): Post
    suspend fun create(post: PostCreateDto): Post
    suspend fun findAll(
        since: Instant? = null,
        until: Instant? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = 10,
        userId: Long? = null
    ): List<Post>

    suspend fun findById(id: String): Post

    /**
     * 権限を考慮して投稿を取得します。
     *
     * @param id
     * @param userId
     * @return
     */
    suspend fun findByIdForUser(id: Long, userId: Long?): Post?

    /**
     * 権限を考慮してユーザーの投稿を取得します。
     *
     * @param userId
     * @param forUserId
     * @return
     */
    suspend fun findByUserIdForUser(
        userId: Long,
        since: Instant? = null,
        until: Instant? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = null,
        forUserId: Long? = null
    ): List<Post>

    suspend fun findByUserNameAndDomainForUser(
        userName: String,
        domain: String = Config.configData.domain,
        since: Instant? = null,
        until: Instant? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = null,
        forUserId: Long? = null
    ): List<Post>

    suspend fun delete(id: String)
}
