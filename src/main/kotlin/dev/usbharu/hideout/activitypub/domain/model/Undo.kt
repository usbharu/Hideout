package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Undo(
    type: List<String> = emptyList(),
    override val actor: String,
    override val id: String,
    @JsonDeserialize(using = ObjectDeserializer::class)
    @JsonProperty("object") val apObject: Object,
    val published: String?
) : Object(add(type, "Undo")), HasId, HasActor {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Undo

        if (actor != other.actor) return false
        if (id != other.id) return false
        if (apObject != other.apObject) return false
        if (published != other.published) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + actor.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + apObject.hashCode()
        result = 31 * result + (published?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Undo(" +
            "actor='$actor', " +
            "id='$id', " +
            "apObject=$apObject, " +
            "published=$published" +
            ")" +
            " ${super.toString()}"
    }
}
