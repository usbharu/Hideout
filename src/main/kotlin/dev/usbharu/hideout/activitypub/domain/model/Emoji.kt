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

    override fun toString(): String {
        return "Emoji(" +
                "name='$name', " +
                "id='$id', " +
                "updated='$updated', " +
                "icon=$icon" +
                ")" +
                " ${super.toString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Emoji

        if (name != other.name) return false
        if (id != other.id) return false
        if (updated != other.updated) return false
        if (icon != other.icon) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + updated.hashCode()
        result = 31 * result + icon.hashCode()
        return result
    }
}
