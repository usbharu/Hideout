package dev.usbharu.hideout.core.domain.model.filter

data class Filter(
    val id: Long,
    val userId: Long,
    val name: String,
    val context: List<FilterType>,
    val filterAction: FilterAction,
)
