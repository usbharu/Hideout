package dev.usbharu.hideout.core.domain.model.filter

import dev.usbharu.hideout.core.domain.model.filter.FilterMode.*

class FilterKeyword(
    val id: FilterKeywordId,
    var keyword: FilterKeywordKeyword,
    val mode: FilterMode
) {
    fun match(string: String): Boolean {
        when (mode) {
            WHOLE_WORD -> TODO()
            REGEX -> TODO()
            NONE -> TODO()
        }
    }
}
