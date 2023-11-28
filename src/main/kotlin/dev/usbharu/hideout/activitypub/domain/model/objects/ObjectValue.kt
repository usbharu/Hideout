package dev.usbharu.hideout.activitypub.domain.model.objects

import com.fasterxml.jackson.annotation.JsonCreator

@Suppress("VariableNaming")
open class ObjectValue @JsonCreator constructor(type: List<String>, var `object`: String) : Object(
    type
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ObjectValue

        return `object` == other.`object`
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + `object`.hashCode()
        return result
    }

    override fun toString(): String = "ObjectValue(`object`='$`object`') ${super.toString()}"
}
