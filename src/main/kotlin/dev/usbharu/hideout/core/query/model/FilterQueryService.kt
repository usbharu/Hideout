package dev.usbharu.hideout.core.query.model

import dev.usbharu.hideout.core.domain.model.filter.FilterType

interface FilterQueryService {
    suspend fun findByUserIdAndType(userId: Long, types: List<FilterType>): List<FilterQueryModel>
    suspend fun findByUserId(userId: Long): List<FilterQueryModel>
    suspend fun findByUserIdAndId(userId: Long, id: Long): FilterQueryModel?
    suspend fun findByUserIdAndKeywordId(userId: Long, keywordId: Long): FilterQueryModel?
}