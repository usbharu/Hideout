package dev.usbharu.hideout.service.api.mastodon

import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment
import dev.usbharu.hideout.domain.model.hideout.form.Media
import dev.usbharu.hideout.service.media.MediaService

class MediaApiServiceImpl(private val mediaService: MediaService) : MediaApiService {
    override suspend fun postMedia(media: Media): MediaAttachment {
        mediaService.uploadLocalMedia(media)
    }
}
