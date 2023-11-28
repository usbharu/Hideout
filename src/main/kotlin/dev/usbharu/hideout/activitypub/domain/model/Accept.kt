package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Accept @JsonCreator constructor(
    type: List<String> = emptyList(),
    override val name: String,
    @JsonDeserialize(using = ObjectDeserializer::class) @Suppress("VariableNaming") var `object`: Object?,
    override val actor: String
) : Object(
    type = add(type, "Accept")
),
    HasActor,
    HasName {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Accept

        if (`object` != other.`object`) return false
        if (actor != other.actor) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        result = 31 * result + actor.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String {
        return "Accept(" + "`object`=$`object`, " + "actor='$actor', " + "name='$name'" + ")" + " ${super.toString()}"
    }
}
