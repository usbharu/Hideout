package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.infrastructure.exposedrepository.Media
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toMedia
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import dev.usbharu.hideout.core.domain.model.media.Media as MediaEntity

@Repository
class MediaQueryServiceImpl : MediaQueryService {

    override suspend fun findByRemoteUrl(remoteUrl: String): MediaEntity? {
        return Media.select { Media.remoteUrl eq remoteUrl }.forUpdate()
            .singleOrNull()
            ?.toMedia()
    }
}
