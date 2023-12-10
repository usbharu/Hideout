package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Create(
    type: List<String> = emptyList(),
    val name: String? = null,
    @JsonDeserialize(using = ObjectDeserializer::class)
    @JsonProperty("object")
    val apObject: Object,
    override val actor: String,
    override val id: String,
    val to: List<String> = emptyList(),
    val cc: List<String> = emptyList()
) : Object(
    type = add(type, "Create")
),
    HasId,
    HasActor {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Create

        if (name != other.name) return false
        if (apObject != other.apObject) return false
        if (actor != other.actor) return false
        if (id != other.id) return false
        if (to != other.to) return false
        if (cc != other.cc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + apObject.hashCode()
        result = 31 * result + actor.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + cc.hashCode()
        return result
    }

    override fun toString(): String {
        return "Create(" +
            "name=$name, " +
            "apObject=$apObject, " +
            "actor='$actor', " +
            "id='$id', " +
            "to=$to, " +
            "cc=$cc" +
            ")" +
            " ${super.toString()}"
    }
}
