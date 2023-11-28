package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Follow(
    type: List<String> = emptyList(),
    @Suppress("VariableNaming") val `object`: String,
    override val actor: String
) : Object(
    type = add(type, "Follow")
),
    HasActor {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Follow

        if (`object` != other.`object`) return false
        if (actor != other.actor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        result = 31 * result + actor.hashCode()
        return result
    }

    override fun toString(): String = "Follow(`object`=$`object`, actor='$actor') ${super.toString()}"
}
