package dev.usbharu.hideout.core.service.media

import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.mastodon.interfaces.api.media.MediaRequest

interface MediaService {
    suspend fun uploadLocalMedia(mediaRequest: MediaRequest): Media
    suspend fun uploadRemoteMedia(remoteMedia: RemoteMedia): Media
}
