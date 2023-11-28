package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Undo(
    type: List<String> = emptyList(),
    override val actor: String,
    override val id: String,
    @JsonDeserialize(using = ObjectDeserializer::class)
    @Suppress("VariableNaming") val `object`: Object,
    val published: String
) : Object(add(type, "Undo")), HasId, HasActor {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Undo

        if (`object` != other.`object`) return false
        if (published != other.published) return false
        if (actor != other.actor) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        result = 31 * result + (published?.hashCode() ?: 0)
        result = 31 * result + actor.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "Undo(`object`=$`object`, published=$published, actor='$actor', id='$id') ${super.toString()}"
    }
}
