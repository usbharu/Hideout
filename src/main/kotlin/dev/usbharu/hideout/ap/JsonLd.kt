package dev.usbharu.hideout.ap

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
open class JsonLd {
    @JsonProperty("@context")
    @JsonDeserialize(using = ContextDeserializer::class)
    var context:List<String> = emptyList()

    @JsonCreator
    constructor(context:List<String>){
        this.context = context
    }

    protected constructor()
}

public class ContextDeserializer : JsonDeserializer<List<String>>() {
    override fun deserialize(p0: com.fasterxml.jackson.core.JsonParser?, p1: com.fasterxml.jackson.databind.DeserializationContext?): List<String> {
        return emptyList()
    }
}
