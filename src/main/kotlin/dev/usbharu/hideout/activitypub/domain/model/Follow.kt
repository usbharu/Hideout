package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Follow : Object, HasActor {
    @Suppress("VariableNaming")
    var `object`: String? = null

    override val actor: String

    constructor(
        type: List<String> = emptyList(),
        `object`: String?,
        actor: String
    ) : super(
        type = add(type, "Follow")
    ) {
        this.`object` = `object`
        this.actor = actor
    }

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
