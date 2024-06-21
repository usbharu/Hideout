package dev.usbharu.hideout.core.domain.model.filter

@JvmInline
value class FilterId(val id: Long) {
    init {
        require(0 <= id)
    }
}
