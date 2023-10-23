package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import org.jetbrains.exposed.sql.ResultRow
import org.springframework.stereotype.Component

@Component
class PostResultRowMapper(private val postBuilder: Post.PostBuilder) : ResultRowMapper<Post> {
    override fun map(resultRow: ResultRow): Post {
        return postBuilder.of(
            id = resultRow[Posts.id],
            userId = resultRow[Posts.userId],
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
