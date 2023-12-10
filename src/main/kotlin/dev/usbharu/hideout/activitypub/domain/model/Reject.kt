package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Reject(
    override val actor: String,
    override val id: String,
    @JsonDeserialize(using = ObjectDeserializer::class) @JsonProperty("object") val apObject: Object
) : Object(listOf("Reject")), HasId, HasActor {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Reject

        if (actor != other.actor) return false
        if (id != other.id) return false
        if (apObject != other.apObject) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + actor.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + apObject.hashCode()
        return result
    }

    override fun toString(): String {
        return "Reject(" +
            "actor='$actor', " +
            "id='$id', " +
            "apObject=$apObject" +
            ")" +
            " ${super.toString()}"
    }
}
