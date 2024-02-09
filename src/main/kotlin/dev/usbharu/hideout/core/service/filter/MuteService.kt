package dev.usbharu.hideout.core.service.filter

import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterAction
import dev.usbharu.hideout.core.domain.model.filter.FilterType
import dev.usbharu.hideout.core.query.model.FilterQueryModel

interface MuteService {
    suspend fun createFilter(
        title: String,
        name: String,
        context: List<FilterType>,
        action: FilterAction,
        keywords: List<FilterKeyword>,
        loginUser: Long
    ): Filter

    suspend fun getFilters(userId: Long, types: List<FilterType> = emptyList()): List<FilterQueryModel>

    suspend fun deleteFilter(filterId: Long)
}