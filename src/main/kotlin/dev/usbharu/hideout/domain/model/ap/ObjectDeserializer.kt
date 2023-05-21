package dev.usbharu.hideout.domain.model.ap

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import dev.usbharu.hideout.service.activitypub.ActivityType

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
                    ActivityType.values().firstOrNull { it.name.equals(jsonNode.asText(), true) }
                }
            } else if (type.isValueNode) {
                ActivityType.values().first { it.name.equals(type.asText(), true) }
            } else {
                TODO()
            }

            return when (activityType) {
                ActivityType.Follow -> {
                    val readValue = p.codec.treeToValue(treeNode, Follow::class.java)
                    println(readValue)
                    readValue
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
