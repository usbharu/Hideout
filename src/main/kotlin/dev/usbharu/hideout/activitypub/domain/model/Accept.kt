package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Accept @JsonCreator constructor(
    type: List<String> = emptyList(),
    @JsonDeserialize(using = ObjectDeserializer::class)
    @JsonProperty("object")
    val apObject: Object,
    override val actor: String
) : Object(
    type = add(type, "Accept")
),
    HasActor {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Accept

        if (apObject != other.apObject) return false
        if (actor != other.actor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + apObject.hashCode()
        result = 31 * result + actor.hashCode()
        return result
    }

    override fun toString(): String {
        return "Accept(" +
                "apObject=$apObject, " +
                "actor='$actor'" +
                ")" +
                " ${super.toString()}"
    }
}
