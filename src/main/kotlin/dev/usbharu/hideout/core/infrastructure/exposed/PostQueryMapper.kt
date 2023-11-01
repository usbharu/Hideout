package dev.usbharu.hideout.core.infrastructure.exposed

import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts
import dev.usbharu.hideout.core.infrastructure.exposedrepository.PostsMedia
import org.jetbrains.exposed.sql.Query
import org.springframework.stereotype.Component

@Component
class PostQueryMapper(private val postResultRowMapper: ResultRowMapper<Post>) : QueryMapper<Post> {
    override fun map(query: Query): List<Post> {
        return query.groupBy { it[Posts.id] }
            .map { it.value }
            .map {
                it.first().let(postResultRowMapper::map)
                    .copy(mediaIds = it.mapNotNull { it.getOrNull(PostsMedia.mediaId) })
            }
    }
}
