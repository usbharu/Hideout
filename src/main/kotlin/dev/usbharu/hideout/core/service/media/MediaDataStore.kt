package dev.usbharu.hideout.core.service.media

interface MediaDataStore {
    suspend fun save(dataMediaSave: MediaSave): SavedMedia
    suspend fun delete(id: String)
}