package dev.usbharu.hideout.domain.model.ap

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.Instant

class Undo : Object {

    @JsonDeserialize(using = ObjectDeserializer::class)
    var `object`: Object? = null
    var published: String? = null

    protected constructor()
    constructor(
        type: List<String> = emptyList(),
        name: String,
        actor: String,
        id: String?,
        `object`: Object,
        published: Instant
    ) : super(add(type, "Undo"), name, actor, id) {
        this.`object` = `object`
        this.published = published.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Undo) return false
        if (!super.equals(other)) return false

        if (`object` != other.`object`) return false
        return published == other.published
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        result = 31 * result + (published?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Undo(`object`=$`object`, published=$published) ${super.toString()}"
    }


}
