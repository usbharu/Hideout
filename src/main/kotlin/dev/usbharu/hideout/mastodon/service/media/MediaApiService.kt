package dev.usbharu.hideout.mastodon.service.media

import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment
import dev.usbharu.hideout.mastodon.interfaces.api.media.MediaRequest
import org.springframework.stereotype.Service

@Service
interface MediaApiService {
    suspend fun postMedia(mediaRequest: MediaRequest): MediaAttachment
}
