package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Follow : Object {
    @Suppress("VariableNaming")
    var `object`: String? = null

    protected constructor() : super()
    constructor(
        type: List<String> = emptyList(),
        name: String?,
        `object`: String?,
        actor: String?
    ) : super(
        type = add(type, "Follow"),
        name = name,
        actor = actor
    ) {
        this.`object` = `object`
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Follow) return false
        if (!super.equals(other)) return false

        return `object` == other.`object`
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Follow(`object`=$`object`) ${super.toString()}"
}
