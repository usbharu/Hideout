package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Like : Object, HasId, HasActor {
    @Suppress("VariableNaming")
    var `object`: String? = null
    var content: String? = null

    @JsonDeserialize(contentUsing = ObjectDeserializer::class)
    var tag: List<Object> = emptyList()
    override val actor: String
    override val id: String

    constructor(
        type: List<String> = emptyList(),
        actor: String,
        id: String,
        `object`: String?,
        content: String?,
        tag: List<Object> = emptyList()
    ) : super(
        type = add(type, "Like")
    ) {
        this.`object` = `object`
        this.content = content
        this.tag = tag
        this.actor = actor
        this.id = id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Like

        if (`object` != other.`object`) return false
        if (content != other.content) return false
        if (tag != other.tag) return false
        if (actor != other.actor) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + tag.hashCode()
        result = 31 * result + actor.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "Like(`object`=$`object`, content=$content, tag=$tag, actor='$actor', id='$id') ${super.toString()}"
    }
}
