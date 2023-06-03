package dev.usbharu.hideout.domain.model.ap

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import dev.usbharu.hideout.service.activitypub.ActivityVocabulary

class ObjectDeserializer : JsonDeserializer<Object>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Object {
        requireNotNull(p)
        val treeNode: JsonNode = requireNotNull(p.codec?.readTree(p))
        if (treeNode.isValueNode) {
            return ObjectValue(
                emptyList(),
                null,
                null,
                null,
                treeNode.asText()
            )
        } else if (treeNode.isObject) {
            val type = treeNode["type"]
            val activityType = if (type.isArray) {
                type.firstNotNullOf { jsonNode: JsonNode ->
                    ActivityVocabulary.values().firstOrNull { it.name.equals(jsonNode.asText(), true) }
                }
            } else if (type.isValueNode) {
                ActivityVocabulary.values().first { it.name.equals(type.asText(), true) }
            } else {
                TODO()
            }

            return when (activityType) {
                ActivityVocabulary.Follow -> {
                    val readValue = p.codec.treeToValue(treeNode, Follow::class.java)
                    println(readValue)
                    readValue
                }

                ActivityVocabulary.Note -> {
                    p.codec.treeToValue(treeNode, Note::class.java)
                }

                else -> {
                    TODO()
                }
            }
        } else {
            TODO()
        }
    }
}
