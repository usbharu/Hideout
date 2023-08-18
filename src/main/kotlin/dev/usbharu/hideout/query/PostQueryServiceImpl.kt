package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.exception.FailedToGetResourcesException
import dev.usbharu.hideout.repository.Posts
import dev.usbharu.hideout.repository.toPost
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single
import org.springframework.stereotype.Repository

@Single
@Repository
class PostQueryServiceImpl : PostQueryService {
    override suspend fun findById(id: Long): Post =
        Posts.select { Posts.id eq id }
            .singleOr { FailedToGetResourcesException("id: $id is duplicate or does not exist.", it) }.toPost()

    override suspend fun findByUrl(url: String): Post = Posts.select { Posts.url eq url }
        .singleOr { FailedToGetResourcesException("url: $url is duplicate or does not exist.", it) }.toPost()

    override suspend fun findByApId(string: String): Post = Posts.select { Posts.apId eq string }
        .singleOr { FailedToGetResourcesException("apId: $string is duplicate or does not exist.", it) }.toPost()
}
