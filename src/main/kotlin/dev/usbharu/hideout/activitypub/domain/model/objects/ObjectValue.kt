package dev.usbharu.hideout.activitypub.domain.model.objects

import com.fasterxml.jackson.annotation.JsonCreator

@Suppress("VariableNaming")
open class ObjectValue : Object {

    lateinit var `object`: String

    @JsonCreator
    constructor(type: List<String>) : super(
        type
    ) {
        this.`object` = `object`
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjectValue) return false
        if (!super.equals(other)) return false

        return `object` == other.`object`
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "ObjectValue(`object`=$`object`) ${super.toString()}"
}
