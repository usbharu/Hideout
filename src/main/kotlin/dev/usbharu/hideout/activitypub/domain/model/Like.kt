package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Like : Object {
    var `object`: String? = null
    var content: String? = null

    @JsonDeserialize(contentUsing = ObjectDeserializer::class)
    var tag: List<Object> = emptyList()

    protected constructor() : super()
    constructor(
        type: List<String> = emptyList(),
        name: String?,
        actor: String?,
        id: String?,
        `object`: String?,
        content: String?,
        tag: List<Object> = emptyList()
    ) : super(
        type = add(type, "Like"),
        name = name,
        actor = actor,
        id = id
    ) {
        this.`object` = `object`
        this.content = content
        this.tag = tag
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Like) return false
        if (!super.equals(other)) return false

        if (`object` != other.`object`) return false
        if (content != other.content) return false
        return tag == other.tag
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + tag.hashCode()
        return result
    }

    override fun toString(): String = "Like(`object`=$`object`, content=$content, tag=$tag) ${super.toString()}"
}
