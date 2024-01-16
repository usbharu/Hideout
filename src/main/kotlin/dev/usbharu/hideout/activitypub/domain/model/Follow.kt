package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Follow(
    type: List<String> = emptyList(),
    @JsonProperty("object") val apObject: String,
    override val actor: String,
    val id: String? = null
) : Object(
    type = add(type, "Follow")
),
    HasActor {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Follow

        if (apObject != other.apObject) return false
        if (actor != other.actor) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + apObject.hashCode()
        result = 31 * result + actor.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Follow(" +
                "apObject='$apObject', " +
                "actor='$actor', " +
                "id=$id" +
                ")" +
                " ${super.toString()}"
    }
}
