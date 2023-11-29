package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Delete : Object, HasId, HasActor {
    @JsonDeserialize(using = ObjectDeserializer::class)
    @JsonProperty("object")
    val apObject: Object
    val published: String
    override val actor: String
    override val id: String

    constructor(
        type: List<String> = emptyList(),
        actor: String,
        id: String,
        `object`: Object,
        published: String
    ) : super(add(type, "Delete")) {
        this.apObject = `object`
        this.published = published
        this.actor = actor
        this.id = id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Delete

        if (apObject != other.apObject) return false
        if (published != other.published) return false
        if (actor != other.actor) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + apObject.hashCode()
        result = 31 * result + published.hashCode()
        result = 31 * result + actor.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String =
        "Delete(`object`=$apObject, published=$published, actor='$actor', id='$id') ${super.toString()}"
}
