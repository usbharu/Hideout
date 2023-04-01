package dev.usbharu.hideout.ap

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.TreeNode
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
    @JsonSerialize(using = ContextSerializer::class)
    var context:List<String> = emptyList()

    @JsonCreator
    constructor(context:List<String>){
        this.context = context
    }

    protected constructor()
}

public class ContextDeserializer : JsonDeserializer<String>() {
    override fun deserialize(p0: com.fasterxml.jackson.core.JsonParser?, p1: com.fasterxml.jackson.databind.DeserializationContext?): String {
        val readTree : JsonNode  = p0?.codec?.readTree(p0) ?: return ""
        if (readTree.isObject) {
            return ""
        }
        return readTree.asText()
    }
}

public class ContextSerializer : JsonSerializer<List<String>>() {
    override fun serialize(value: List<String>?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        if (value.isNullOrEmpty()) {
            gen?.writeNull()
            return
        }
        if (value?.size == 1) {
            gen?.writeString(value[0])
        } else {
            gen?.writeStartArray()
            value?.forEach {
                gen?.writeString(it)
            }
            gen?.writeEndArray()
        }
    }

}
