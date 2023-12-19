package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts
import dev.usbharu.hideout.core.infrastructure.exposedrepository.PostsMedia
import dev.usbharu.hideout.core.query.PostQueryService
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository

@Repository
class PostQueryServiceImpl(
    private val postResultRowMapper: ResultRowMapper<Post>,
    private val postQueryMapper: QueryMapper<Post>
) : PostQueryService {
    override suspend fun findById(id: Long): Post? =
        Posts.leftJoin(PostsMedia)
            .select { Posts.id eq id }
            .singleOrNull()
            ?.let(postResultRowMapper::map)

    override suspend fun findByUrl(url: String): Post? =
        Posts.leftJoin(PostsMedia)
            .select { Posts.url eq url }
            .let(postQueryMapper::map)
            .singleOrNull()

    override suspend fun findByApId(string: String): Post? =
        Posts.leftJoin(PostsMedia)
            .select { Posts.apId eq string }
            .let(postQueryMapper::map)
            .singleOrNull()

    override suspend fun findByActorId(actorId: Long): List<Post> =
        Posts.leftJoin(PostsMedia).select { Posts.actorId eq actorId }.let(postQueryMapper::map)
}
