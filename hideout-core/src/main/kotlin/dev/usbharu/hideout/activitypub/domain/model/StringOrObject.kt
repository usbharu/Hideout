package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*

open class StringOrObject {
    var contextString: String? = null
    var contextObject: Map<String, String>? = null

    @JsonCreator
    protected constructor()

    constructor(string: String) : this() {
        contextString = string
    }

    constructor(contextObject: Map<String, String>) : this() {
        this.contextObject = contextObject
    }

    fun isEmpty(): Boolean = contextString.isNullOrEmpty() and contextObject.isNullOrEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringOrObject

        if (contextString != other.contextString) return false
        if (contextObject != other.contextObject) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contextString?.hashCode() ?: 0
        result = 31 * result + (contextObject?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "StringOrObject(contextString=$contextString, contextObject=$contextObject)"
    }


}


class StringOrObjectDeserializer : JsonDeserializer<StringOrObject>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext): StringOrObject {
        val readTree: JsonNode = p?.codec?.readTree(p) ?: return StringOrObject("")
        return if (readTree.isValueNode) {
            StringOrObject(readTree.textValue())
        } else if (readTree.isObject) {
            val map: Map<String, String> = ctxt.readTreeAsValue(
                readTree,
                ctxt.typeFactory.constructType(object : TypeReference<Map<String, String>>() {})
            )
            StringOrObject(map)
        } else {
            StringOrObject("")
        }
    }

}

class StringORObjectSerializer : JsonSerializer<StringOrObject>() {
    override fun serialize(value: StringOrObject?, gen: JsonGenerator?, serializers: SerializerProvider) {
        if (value == null) {
            serializers.defaultSerializeNull(gen)
            return
        }
        if (value.contextString != null) {
            gen?.writeString(value.contextString)
        } else {
            serializers.defaultSerializeValue(value.contextObject, gen)
        }
    }
}
