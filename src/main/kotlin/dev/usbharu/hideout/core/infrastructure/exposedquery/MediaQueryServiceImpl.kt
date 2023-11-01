package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.core.infrastructure.exposedrepository.PostsMedia
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toMedia
import dev.usbharu.hideout.core.query.MediaQueryService
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository

@Repository
class MediaQueryServiceImpl : MediaQueryService {
    override suspend fun findByPostId(postId: Long): List<Media> {
        return dev.usbharu.hideout.core.infrastructure.exposedrepository.Media.innerJoin(
            PostsMedia,
            onColumn = { id },
            otherColumn = { mediaId }
        )
            .select { PostsMedia.postId eq postId }
            .map { it.toMedia() }
    }
}
