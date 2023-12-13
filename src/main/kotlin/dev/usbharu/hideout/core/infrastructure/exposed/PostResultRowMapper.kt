package dev.usbharu.hideout.core.infrastructure.exposed

import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts
import org.jetbrains.exposed.sql.ResultRow
import org.springframework.stereotype.Component

@Component
class PostResultRowMapper(private val postBuilder: Post.PostBuilder) : ResultRowMapper<Post> {
    override fun map(resultRow: ResultRow): Post {
        if (resultRow[Posts.deleted]) {
            return postBuilder.deleteOf(
                resultRow[Posts.id],
                Visibility.values().first { it.ordinal == resultRow[Posts.visibility] },
                url = resultRow[Posts.url],
                repostId = resultRow[Posts.repostId],
                replyId = resultRow[Posts.replyId],
                apId = resultRow[Posts.apId]
            )
        }

        return postBuilder.of(
            id = resultRow[Posts.id],
            actorId = resultRow[Posts.actorId],
            overview = resultRow[Posts.overview],
            text = resultRow[Posts.text],
            createdAt = resultRow[Posts.createdAt],
            visibility = Visibility.values().first { visibility -> visibility.ordinal == resultRow[Posts.visibility] },
            url = resultRow[Posts.url],
            repostId = resultRow[Posts.repostId],
            replyId = resultRow[Posts.replyId],
            sensitive = resultRow[Posts.sensitive],
            apId = resultRow[Posts.apId],
        )
    }
}
