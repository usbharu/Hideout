package dev.usbharu.hideout.service.media

import dev.usbharu.hideout.domain.model.hideout.dto.RemoteMedia
import dev.usbharu.hideout.domain.model.hideout.form.Media

interface MediaService {
    suspend fun uploadLocalMedia(media: Media): dev.usbharu.hideout.domain.model.hideout.entity.Media
    suspend fun uploadRemoteMedia(remoteMedia: RemoteMedia)
}
