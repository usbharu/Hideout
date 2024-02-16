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
        @Suppress("FunctionMinLength")
        fun of(filter: Filter, keywords: List<FilterKeyword>): FilterQueryModel = FilterQueryModel(
            id = filter.id,
            userId = filter.userId,
            name = filter.name,
            context = filter.context,
            filterAction = filter.filterAction,
            keywords = keywords
        )
    }
}
