package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Delete : Object, HasId, HasActor, HasName {
    @JsonDeserialize(using = ObjectDeserializer::class)
    @Suppress("VariableNaming")
    var `object`: Object? = null
    var published: String? = null
    override val actor: String
    override val id: String
    override val name: String

    constructor(
        type: List<String> = emptyList(),
        name: String = "Delete",
        actor: String,
        id: String,
        `object`: Object,
        published: String?
    ) : super(add(type, "Delete")) {
        this.`object` = `object`
        this.published = published
        this.name = name
        this.actor = actor
        this.id = id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Delete

        if (`object` != other.`object`) return false
        if (published != other.published) return false
        if (actor != other.actor) return false
        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        result = 31 * result + (published?.hashCode() ?: 0)
        result = 31 * result + actor.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String =
        "Delete(`object`=$`object`, published=$published, actor='$actor', id='$id', name='$name') ${super.toString()}"
}
