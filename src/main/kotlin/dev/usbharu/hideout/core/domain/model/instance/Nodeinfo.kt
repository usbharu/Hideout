package dev.usbharu.hideout.core.domain.model.instance

class Nodeinfo private constructor() {

    var links: List<Links> = emptyList()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Nodeinfo

        return links == other.links
    }

    override fun hashCode(): Int = links.hashCode()
}

class Links private constructor() {
    var rel: String? = null
    var href: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Links

        if (rel != other.rel) return false
        if (href != other.href) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rel?.hashCode() ?: 0
        result = 31 * result + (href?.hashCode() ?: 0)
        return result
    }
}
