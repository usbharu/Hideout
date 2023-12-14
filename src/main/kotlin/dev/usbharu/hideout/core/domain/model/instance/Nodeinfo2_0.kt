@file:Suppress("Filename")

package dev.usbharu.hideout.core.domain.model.instance

@Suppress("ClassNaming")
class Nodeinfo2_0 {
    var metadata: Metadata? = null
    var software: Software? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Nodeinfo2_0

        if (metadata != other.metadata) return false
        if (software != other.software) return false

        return true
    }

    override fun hashCode(): Int {
        var result = metadata?.hashCode() ?: 0
        result = 31 * result + (software?.hashCode() ?: 0)
        return result
    }
}

class Metadata {
    var nodeName: String? = null
    var nodeDescription: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Metadata

        if (nodeName != other.nodeName) return false
        if (nodeDescription != other.nodeDescription) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nodeName?.hashCode() ?: 0
        result = 31 * result + (nodeDescription?.hashCode() ?: 0)
        return result
    }
}

class Software {
    var name: String? = null
    var version: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Software

        if (name != other.name) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (version?.hashCode() ?: 0)
        return result
    }
}
