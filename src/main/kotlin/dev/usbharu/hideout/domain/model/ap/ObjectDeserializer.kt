package dev.usbharu.hideout.domain.model.ap

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import dev.usbharu.hideout.service.ap.ExtendedActivityVocabulary

class ObjectDeserializer : JsonDeserializer<Object>() {
    @Suppress("LongMethod", "CyclomaticComplexMethod")
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
                    ExtendedActivityVocabulary.values().firstOrNull { it.name.equals(jsonNode.asText(), true) }
                }
            } else if (type.isValueNode) {
                ExtendedActivityVocabulary.values().first { it.name.equals(type.asText(), true) }
            } else {
                TODO()
            }

            return when (activityType) {
                ExtendedActivityVocabulary.Follow -> {
                    val readValue = p.codec.treeToValue(treeNode, Follow::class.java)
                    println(readValue)
                    readValue
                }

                ExtendedActivityVocabulary.Note -> {
                    p.codec.treeToValue(treeNode, Note::class.java)
                }

                ExtendedActivityVocabulary.Object -> p.codec.treeToValue(treeNode, Object::class.java)
                ExtendedActivityVocabulary.Link -> TODO()
                ExtendedActivityVocabulary.Activity -> TODO()
                ExtendedActivityVocabulary.IntransitiveActivity -> TODO()
                ExtendedActivityVocabulary.Collection -> TODO()
                ExtendedActivityVocabulary.OrderedCollection -> TODO()
                ExtendedActivityVocabulary.CollectionPage -> TODO()
                ExtendedActivityVocabulary.OrderedCollectionPage -> TODO()
                ExtendedActivityVocabulary.Accept -> p.codec.treeToValue(treeNode, Accept::class.java)
                ExtendedActivityVocabulary.Add -> TODO()
                ExtendedActivityVocabulary.Announce -> TODO()
                ExtendedActivityVocabulary.Arrive -> TODO()
                ExtendedActivityVocabulary.Block -> TODO()
                ExtendedActivityVocabulary.Create -> p.codec.treeToValue(treeNode, Create::class.java)
                ExtendedActivityVocabulary.Delete -> TODO()
                ExtendedActivityVocabulary.Dislike -> TODO()
                ExtendedActivityVocabulary.Flag -> TODO()
                ExtendedActivityVocabulary.Ignore -> TODO()
                ExtendedActivityVocabulary.Invite -> TODO()
                ExtendedActivityVocabulary.Join -> TODO()
                ExtendedActivityVocabulary.Leave -> TODO()
                ExtendedActivityVocabulary.Like -> p.codec.treeToValue(treeNode, Like::class.java)
                ExtendedActivityVocabulary.Listen -> TODO()
                ExtendedActivityVocabulary.Move -> TODO()
                ExtendedActivityVocabulary.Offer -> TODO()
                ExtendedActivityVocabulary.Question -> TODO()
                ExtendedActivityVocabulary.Reject -> TODO()
                ExtendedActivityVocabulary.Read -> TODO()
                ExtendedActivityVocabulary.Remove -> TODO()
                ExtendedActivityVocabulary.TentativeReject -> TODO()
                ExtendedActivityVocabulary.TentativeAccept -> TODO()
                ExtendedActivityVocabulary.Travel -> TODO()
                ExtendedActivityVocabulary.Undo -> p.codec.treeToValue(treeNode, Undo::class.java)
                ExtendedActivityVocabulary.Update -> TODO()
                ExtendedActivityVocabulary.View -> TODO()
                ExtendedActivityVocabulary.Application -> TODO()
                ExtendedActivityVocabulary.Group -> TODO()
                ExtendedActivityVocabulary.Organization -> TODO()
                ExtendedActivityVocabulary.Person -> p.codec.treeToValue(treeNode, Person::class.java)
                ExtendedActivityVocabulary.Service -> TODO()
                ExtendedActivityVocabulary.Article -> TODO()
                ExtendedActivityVocabulary.Audio -> TODO()
                ExtendedActivityVocabulary.Document -> p.codec.treeToValue(treeNode, Document::class.java)
                ExtendedActivityVocabulary.Event -> TODO()
                ExtendedActivityVocabulary.Image -> p.codec.treeToValue(treeNode, Image::class.java)
                ExtendedActivityVocabulary.Page -> TODO()
                ExtendedActivityVocabulary.Place -> TODO()
                ExtendedActivityVocabulary.Profile -> TODO()
                ExtendedActivityVocabulary.Relationship -> TODO()
                ExtendedActivityVocabulary.Tombstone -> TODO()
                ExtendedActivityVocabulary.Video -> TODO()
                ExtendedActivityVocabulary.Mention -> TODO()
                ExtendedActivityVocabulary.Emoji -> p.codec.treeToValue(treeNode, Emoji::class.java)
            }
        } else {
            TODO()
        }
    }
}
