package dev.usbharu.hideout.service.media

import dev.usbharu.hideout.domain.model.MediaSave
import dev.usbharu.hideout.domain.model.hideout.dto.SavedMedia

interface MediaDataStore {
    suspend fun save(dataMediaSave: MediaSave): SavedMedia
    suspend fun delete(id: Long)
}
