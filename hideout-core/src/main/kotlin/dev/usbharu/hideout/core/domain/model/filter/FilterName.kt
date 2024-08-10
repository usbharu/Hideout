package dev.usbharu.hideout.core.domain.model.filter

class FilterName(name: String) {

    val name = name.take(LENGTH)

    companion object {
        const val LENGTH = 300
    }
}
