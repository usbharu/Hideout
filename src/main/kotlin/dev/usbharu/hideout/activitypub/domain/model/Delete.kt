package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Delete : Object {
    @JsonDeserialize(using = ObjectDeserializer::class)
    var `object`: Object? = null
    var published: String? = null

    constructor(
        type: List<String> = emptyList(),
        name: String? = "Delete",
        actor: String,
        id: String,
        `object`: Object,
        published: String?
    ) : super(add(type, "Delete"), name, actor, id) {
        this.`object` = `object`
        this.published = published
    }

    protected constructor() : super()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Delete) return false
        if (!super.equals(other)) return false

        if (`object` != other.`object`) return false
        if (published != other.published) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        result = 31 * result + (published?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Delete(`object`=$`object`, published=$published) ${super.toString()}"
}
