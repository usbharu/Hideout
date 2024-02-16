package dev.usbharu.hideout.core.service.filter

import dev.usbharu.hideout.core.domain.model.filter.FilterMode

data class FilterKeyword(
    val keyword: String,
    val mode: FilterMode
)
