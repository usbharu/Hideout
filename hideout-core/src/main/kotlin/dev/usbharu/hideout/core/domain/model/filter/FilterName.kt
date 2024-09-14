package dev.usbharu.hideout.core.domain.model.filter

class FilterName(name: String) {

    val name = name.take(LENGTH)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilterName

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    companion object {
        const val LENGTH = 300
    }
}
