package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Post
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
