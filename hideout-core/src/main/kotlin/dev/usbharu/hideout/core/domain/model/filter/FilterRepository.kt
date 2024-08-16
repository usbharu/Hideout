package dev.usbharu.hideout.core.domain.model.filter

import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId

interface FilterRepository {
    suspend fun save(filter: Filter): Filter
    suspend fun delete(filter: Filter)

    suspend fun findByFilterKeywordId(filterKeywordId: FilterKeywordId): Filter?
    suspend fun findByFilterId(filterId: FilterId): Filter?
    suspend fun findByUserDetailId(userDetailId: UserDetailId): List<Filter>
}
