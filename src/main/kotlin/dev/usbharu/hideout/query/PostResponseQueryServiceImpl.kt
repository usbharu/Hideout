package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.dto.PostResponse
import dev.usbharu.hideout.repository.Posts
import dev.usbharu.hideout.repository.Users
import dev.usbharu.hideout.repository.toPost
import dev.usbharu.hideout.repository.toUser
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single

@Single
class PostResponseQueryServiceImpl : PostResponseQueryService {
    override suspend fun findById(id: Long, userId: Long?): PostResponse {
        return Posts
            .innerJoin(Users, onColumn = { Posts.userId }, otherColumn = { Users.id })
            .select { Posts.id eq id }
            .single()
            .let { PostResponse.from(it.toPost(), it.toUser()) }
    }

    override suspend fun findAll(
        since: Long?,
        until: Long?,
        minId: Long?,
        maxId: Long?,
        limit: Int?,
        userId: Long?
    ): List<PostResponse> {
        return Posts
            .innerJoin(Users, onColumn = { Posts.userId }, otherColumn = { id })
            .selectAll()
            .map { PostResponse.from(it.toPost(), it.toUser()) }
    }

    override suspend fun findByUserId(
        userId: Long,
        since: Long?,
        until: Long?,
        minId: Long?,
        maxId: Long?,
        limit: Int?,
        userId2: Long?
    ): List<PostResponse> {
        return Posts
            .innerJoin(Users, onColumn = { Posts.userId }, otherColumn = { id })
            .select { Posts.userId eq userId }
            .map { PostResponse.from(it.toPost(), it.toUser()) }
    }

    override suspend fun findByUserNameAndUserDomain(
        name: String,
        domain: String,
        since: Long?,
        until: Long?,
        minId: Long?,
        maxId: Long?,
        limit: Int?,
        userId: Long?
    ): List<PostResponse> {
        return Posts
            .innerJoin(Users, onColumn = { Posts.userId }, otherColumn = { id })
            .select { Users.name eq name and (Users.domain eq domain) }
            .map { PostResponse.from(it.toPost(), it.toUser()) }
    }
}
