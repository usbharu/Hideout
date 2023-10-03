package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.dto.PostResponse
import dev.usbharu.hideout.exception.FailedToGetResourcesException
import dev.usbharu.hideout.repository.Posts
import dev.usbharu.hideout.repository.Users
import dev.usbharu.hideout.repository.toPost
import dev.usbharu.hideout.repository.toUser
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository

@Repository
class PostResponseQueryServiceImpl : PostResponseQueryService {
    override suspend fun findById(id: Long, userId: Long?): PostResponse {
        return Posts
            .innerJoin(Users, onColumn = { Posts.userId }, otherColumn = { Users.id })
            .select { Posts.id eq id }
            .singleOr { FailedToGetResourcesException("id: $id,userId: $userId is a duplicate or does not exist.", it) }
            .let { PostResponse.from(it.toPost(), it.toUser()) }
    }

}
