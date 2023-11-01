package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectDeserializer

open class Create : Object {
    @JsonDeserialize(using = ObjectDeserializer::class)
    var `object`: Object? = null
    var to: List<String> = emptyList()
    var cc: List<String> = emptyList()

    protected constructor() : super()
    constructor(
        type: List<String> = emptyList(),
        name: String? = null,
        `object`: Object?,
        actor: String? = null,
        id: String? = null,
        to: List<String> = emptyList(),
        cc: List<String> = emptyList()
    ) : super(
        type = add(type, "Create"),
        name = name,
        actor = actor,
        id = id
    ) {
        this.`object` = `object`
        this.to = to
        this.cc = cc
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Create) return false
        if (!super.equals(other)) return false

        return `object` == other.`object`
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Create(`object`=$`object`) ${super.toString()}"
}
