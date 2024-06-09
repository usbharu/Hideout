package dev.usbharu.hideout.core.domain.model.filter

interface FilterRepository {
    suspend fun save(filter: Filter): Filter
    suspend fun delete(filter: Filter)
}