package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Media
import dev.usbharu.hideout.core.infrastructure.exposedrepository.PostsMedia
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toMedia
import dev.usbharu.hideout.core.query.MediaQueryService
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import dev.usbharu.hideout.core.domain.model.media.Media as MediaEntity

@Repository
class MediaQueryServiceImpl : MediaQueryService {
    override suspend fun findByPostId(postId: Long): List<MediaEntity> {
        return Media.innerJoin(
            PostsMedia,
            onColumn = { id },
            otherColumn = { mediaId }
        )
            .select { PostsMedia.postId eq postId }
            .map { it.toMedia() }
    }

    override suspend fun findByRemoteUrl(remoteUrl: String): MediaEntity {
        return Media.select { Media.remoteUrl eq remoteUrl }.forUpdate()
            .singleOr { FailedToGetResourcesException("remoteUrl: $remoteUrl is duplicate or not exist.", it) }
            .toMedia()
    }
}
