package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Emoji(
    type: List<String>,
    override val name: String,
    override val id: String,
    val updated: String,
    val icon: Image
) : Object(
    type = add(type, "Emoji")
),
    HasName,
    HasId {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Emoji) return false
        if (!super.equals(other)) return false

        if (updated != other.updated) return false
        return icon == other.icon
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + updated.hashCode()
        result = 31 * result + icon.hashCode()
        return result
    }

    override fun toString(): String = "Emoji(updated=$updated, icon=$icon) ${super.toString()}"
}
