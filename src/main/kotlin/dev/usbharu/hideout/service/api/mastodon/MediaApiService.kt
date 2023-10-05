package dev.usbharu.hideout.service.api.mastodon

import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment
import dev.usbharu.hideout.domain.model.hideout.form.Media
import org.springframework.stereotype.Service

@Service
interface MediaApiService {
    suspend fun postMedia(media: Media): MediaAttachment
}
