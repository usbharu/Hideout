package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.`object`.Object

open class Emoji : Object {
    var updated: String? = null
    var icon: Image? = null

    protected constructor() : super()
    constructor(
        type: List<String>,
        name: String?,
        actor: String?,
        id: String?,
        updated: String?,
        icon: Image?
    ) : super(
        type = add(type, "Emoji"),
        name = name,
        actor = actor,
        id = id
    ) {
        this.updated = updated
        this.icon = icon
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Emoji) return false
        if (!super.equals(other)) return false

        if (updated != other.updated) return false
        return icon == other.icon
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (updated?.hashCode() ?: 0)
        result = 31 * result + (icon?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Emoji(updated=$updated, icon=$icon) ${super.toString()}"
}
