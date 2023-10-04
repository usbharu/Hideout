package dev.usbharu.hideout.service.media

import dev.usbharu.hideout.domain.model.hideout.form.Media

interface MediaService {
    suspend fun uploadLocalMedia(media: Media): SavedMedia
}
