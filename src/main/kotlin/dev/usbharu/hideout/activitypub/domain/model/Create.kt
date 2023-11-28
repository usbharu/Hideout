package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Create(
    type: List<String> = emptyList(),
    override val name: String,
    @JsonDeserialize(using = ObjectDeserializer::class)
    @Suppress("VariableNaming")
    var `object`: Object?,
    override val actor: String,
    override val id: String,
    var to: List<String> = emptyList(),
    var cc: List<String> = emptyList()
) : Object(
    type = add(type, "Create")
),
    HasId,
    HasName,
    HasActor {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Create

        if (`object` != other.`object`) return false
        if (to != other.to) return false
        if (cc != other.cc) return false
        if (name != other.name) return false
        if (actor != other.actor) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        result = 31 * result + to.hashCode()
        result = 31 * result + cc.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + actor.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String =
        "Create(`object`=$`object`, to=$to, cc=$cc, name='$name', actor='$actor', id='$id') ${super.toString()}"
}
