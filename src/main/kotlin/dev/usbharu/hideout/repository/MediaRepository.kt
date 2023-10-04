package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Media

interface MediaRepository {
    suspend fun generateId(): Long
    suspend fun save(media: Media): Media
    suspend fun findById(id: Long): Media
    suspend fun delete(id: Long)
}
