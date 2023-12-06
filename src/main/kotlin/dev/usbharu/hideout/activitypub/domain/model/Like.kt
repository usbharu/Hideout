package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Like(
    type: List<String> = emptyList(),
    override val actor: String,
    override val id: String,
    @JsonProperty("object") val apObject: String,
    val content: String,
    @JsonDeserialize(contentUsing = ObjectDeserializer::class) val tag: List<Object> = emptyList()
) : Object(
    type = add(type, "Like")
),
    HasId,
    HasActor {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Like

        if (actor != other.actor) return false
        if (id != other.id) return false
        if (apObject != other.apObject) return false
        if (content != other.content) return false
        if (tag != other.tag) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + actor.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + apObject.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + tag.hashCode()
        return result
    }

    override fun toString(): String {
        return "Like(" +
            "actor='$actor', " +
            "id='$id', " +
            "apObject='$apObject', " +
            "content='$content', " +
            "tag=$tag" +
            ")" +
            " ${super.toString()}"
    }
}
