package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.`object`.Object
import dev.usbharu.hideout.activitypub.domain.model.`object`.ObjectDeserializer

open class Accept : Object {
    @JsonDeserialize(using = ObjectDeserializer::class)
    var `object`: Object? = null

    protected constructor()
    constructor(
        type: List<String> = emptyList(),
        name: String,
        `object`: Object?,
        actor: String?
    ) : super(
        type = add(type, "Accept"),
        name = name,
        actor = actor
    ) {
        this.`object` = `object`
    }

    override fun toString(): String = "Accept(`object`=$`object`) ${super.toString()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Accept) return false
        if (!super.equals(other)) return false

        return `object` == other.`object`
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        return result
    }
}
