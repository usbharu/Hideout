package dev.usbharu.hideout.core.domain.model.media

interface MediaRepository {
    suspend fun generateId(): Long
    suspend fun save(media: Media): Media
    suspend fun findById(id: Long): Media?
    suspend fun delete(id: Long)
    suspend fun findByRemoteUrl(remoteUrl: String): Media?
}
