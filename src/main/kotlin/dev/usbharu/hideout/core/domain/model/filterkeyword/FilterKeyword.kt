package dev.usbharu.hideout.core.domain.model.filterkeyword

import dev.usbharu.hideout.core.domain.model.filter.FilterMode

data class FilterKeyword(
    val id: Long,
    val filterId: Long,
    val keyword: String,
    val mode: FilterMode
)
