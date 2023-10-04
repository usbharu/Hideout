package dev.usbharu.hideout.service.media

import dev.usbharu.hideout.domain.model.MediaSave

interface MediaDataStore {
    suspend fun save(dataMediaSave: MediaSave)
    suspend fun delete(id: Long)
}
