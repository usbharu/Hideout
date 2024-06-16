package dev.usbharu.hideout.core.domain.model.filter

interface FilterRepository {
    suspend fun save(filter: Filter): Filter
    suspend fun delete(filter: Filter)

    suspend fun findByFilterKeywordId(filterKeywordId: FilterKeywordId): Filter?
    suspend fun findByFilterId(filterId: FilterId): Filter?
}
