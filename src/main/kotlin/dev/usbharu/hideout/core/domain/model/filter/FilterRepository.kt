package dev.usbharu.hideout.core.domain.model.filter

interface FilterRepository {

    suspend fun generateId(): Long
    suspend fun save(filter: Filter): Filter
    suspend fun findById(id: Long): Filter?

    suspend fun findByUserIdAndType(userId: Long, types: List<FilterType>): List<Filter>
    suspend fun deleteById(id: Long)
}