package dev.usbharu.hideout.core.service.filter

import dev.usbharu.hideout.core.query.model.FilterQueryModel

data class FilterResult(
    val filter: FilterQueryModel,
    val keyword: String,
)
