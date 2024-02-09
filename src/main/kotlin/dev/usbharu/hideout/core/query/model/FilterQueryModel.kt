package dev.usbharu.hideout.core.query.model

import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterAction
import dev.usbharu.hideout.core.domain.model.filter.FilterType
import dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeyword

data class FilterQueryModel(
    val id: Long,
    val userId: Long,
    val name: String,
    val context: List<FilterType>,
    val filterAction: FilterAction,
    val keywords: List<FilterKeyword>
) {
    companion object {
        fun of(filter: Filter, keywords: List<FilterKeyword>): FilterQueryModel = FilterQueryModel(
            filter.id,
            filter.userId,
            filter.name,
            filter.context,
            filter.filterAction,
            keywords
        )
    }
}