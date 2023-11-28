package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Tombstone(type: List<String> = emptyList(), override val id: String) :
    Object(add(type, "Tombstone")),
    HasId {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Tombstone

        return id == other.id
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String = "Tombstone(id='$id') ${super.toString()}"
}
