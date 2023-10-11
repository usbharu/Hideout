package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.Media
import dev.usbharu.hideout.repository.PostsMedia
import dev.usbharu.hideout.repository.toMedia
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository

@Repository
class MediaQueryServiceImpl : MediaQueryService {
    override suspend fun findByPostId(postId: Long): List<Media> {
        return dev.usbharu.hideout.repository.Media.innerJoin(PostsMedia, onColumn = { id }, otherColumn = { mediaId })
            .select { PostsMedia.postId eq postId }
            .map { it.toMedia() }
    }
}
