package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
open class JsonLd {
    @JsonProperty("@context")
    @JsonDeserialize(contentUsing = ContextDeserializer::class)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY, using = ContextSerializer::class)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var context: List<String> = emptyList()

    @JsonCreator
    constructor(context: List<String>) {
        this.context = context
    }

    protected constructor()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonLd) return false

        return context == other.context
    }

    override fun hashCode(): Int = context.hashCode()

    override fun toString(): String = "JsonLd(context=$context)"
}

class ContextDeserializer : JsonDeserializer<String>() {
    override fun deserialize(
        p0: com.fasterxml.jackson.core.JsonParser?,
        p1: com.fasterxml.jackson.databind.DeserializationContext?
    ): String {
        val readTree: JsonNode = p0?.codec?.readTree(p0) ?: return ""
        if (readTree.isObject) {
            return ""
        }
        return readTree.asText()
    }
}

class ContextSerializer : JsonSerializer<List<String>>() {

    override fun isEmpty(value: List<String>?): Boolean = value.isNullOrEmpty()

    override fun serialize(value: List<String>?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        if (value.isNullOrEmpty()) {
            gen?.writeNull()
            return
        }
        if (value.size == 1) {
            gen?.writeString(value[0])
        } else {
            gen?.writeStartArray()
            value.forEach {
                gen?.writeString(it)
            }
            gen?.writeEndArray()
        }
    }
}
