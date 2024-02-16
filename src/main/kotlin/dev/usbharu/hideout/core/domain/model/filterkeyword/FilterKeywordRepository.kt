package dev.usbharu.hideout.core.domain.model.filterkeyword

interface FilterKeywordRepository {
    suspend fun generateId(): Long
    suspend fun save(filterKeyword: FilterKeyword): FilterKeyword
    suspend fun saveAll(filterKeywordList: List<FilterKeyword>)
    suspend fun findById(id: Long): FilterKeyword?
    suspend fun deleteById(id: Long)
    suspend fun deleteByFilterId(filterId: Long)
}
