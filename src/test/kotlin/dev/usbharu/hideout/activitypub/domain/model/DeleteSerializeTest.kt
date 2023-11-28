package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.application.config.ActivityPubConfig
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeleteSerializeTest {
    @Test
    fun Misskeyの発行するJSONをデシリアライズできる() {
        @Language("JSON") val json = """{
  "@context" : [ "https://www.w3.org/ns/activitystreams", "https://w3id.org/security/v1", {
    "manuallyApprovesFollowers" : "as:manuallyApprovesFollowers",
    "sensitive" : "as:sensitive",
    "Hashtag" : "as:Hashtag",
    "quoteUrl" : "as:quoteUrl",
    "toot" : "http://joinmastodon.org/ns#",
    "Emoji" : "toot:Emoji",
    "featured" : "toot:featured",
    "discoverable" : "toot:discoverable",
    "schema" : "http://schema.org#",
    "PropertyValue" : "schema:PropertyValue",
    "value" : "schema:value",
    "misskey" : "https://misskey-hub.net/ns#",
    "_misskey_content" : "misskey:_misskey_content",
    "_misskey_quote" : "misskey:_misskey_quote",
    "_misskey_reaction" : "misskey:_misskey_reaction",
    "_misskey_votes" : "misskey:_misskey_votes",
    "isCat" : "misskey:isCat",
    "vcard" : "http://www.w3.org/2006/vcard/ns#"
  } ],
  "type" : "Delete",
  "actor" : "https://misskey.usbharu.dev/users/97ws8y3rj6",
  "object" : {
    "id" : "https://misskey.usbharu.dev/notes/9lkwqnwqk9",
    "type" : "Tombstone"
  },
  "published" : "2023-11-02T15:30:34.160Z",
  "id" : "https://misskey.usbharu.dev/4b5b6ed5-9269-45f3-8403-cba1e74b4b69"
}
"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<Delete>(json)

        val expected = Delete(
            actor = "https://misskey.usbharu.dev/users/97ws8y3rj6",
            id = "https://misskey.usbharu.dev/4b5b6ed5-9269-45f3-8403-cba1e74b4b69",
            `object` = Tombstone(
                id = "https://misskey.usbharu.dev/notes/9lkwqnwqk9",
            ),
            published = "2023-11-02T15:30:34.160Z",
        )
        expected.context = listOf("https://www.w3.org/ns/activitystreams", "https://w3id.org/security/v1", "")
        assertEquals(expected, readValue)
    }

    @Test
    fun シリアライズできる() {
        val delete = Delete(
            actor = "https://misskey.usbharu.dev/users/97ws8y3rj6",
            id = "https://misskey.usbharu.dev/4b5b6ed5-9269-45f3-8403-cba1e74b4b69",
            `object` = Tombstone(
                id = "https://misskey.usbharu.dev/notes/9lkwqnwqk9",
            ),
            published = "2023-11-02T15:30:34.160Z",
        )


        val objectMapper = ActivityPubConfig().objectMapper()

        val actual = objectMapper.writeValueAsString(delete)
        val expected =
            """{"type":"Delete","actor":"https://misskey.usbharu.dev/users/97ws8y3rj6","id":"https://misskey.usbharu.dev/4b5b6ed5-9269-45f3-8403-cba1e74b4b69","object":{"type":"Tombstone","id":"https://misskey.usbharu.dev/notes/9lkwqnwqk9"},"published":"2023-11-02T15:30:34.160Z"}"""
        assertEquals(expected, actual)
    }
}
