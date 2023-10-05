package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.exception.FailedToGetResourcesException
import dev.usbharu.hideout.repository.Posts
import dev.usbharu.hideout.repository.PostsMedia
import dev.usbharu.hideout.repository.toPost
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository

@Repository
class PostQueryServiceImpl : PostQueryService {
    override suspend fun findById(id: Long): Post =
        Posts.innerJoin(PostsMedia, onColumn = { Posts.id }, otherColumn = { PostsMedia.postId })
            .select { Posts.id eq id }
            .singleOr { FailedToGetResourcesException("id: $id is duplicate or does not exist.", it) }.toPost()

    override suspend fun findByUrl(url: String): Post =
        Posts.innerJoin(PostsMedia, onColumn = { Posts.id }, otherColumn = { PostsMedia.postId })
            .select { Posts.url eq url }
            .toPost()
            .singleOr { FailedToGetResourcesException("url: $url is duplicate or does not exist.", it) }

    override suspend fun findByApId(string: String): Post =
        Posts.innerJoin(PostsMedia, onColumn = { Posts.id }, otherColumn = { PostsMedia.postId })
            .select { Posts.apId eq string }
            .toPost()
            .singleOr { FailedToGetResourcesException("apId: $string is duplicate or does not exist.", it) }
}
