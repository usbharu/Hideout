package dev.usbharu.hideout.domain.model.ap

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Undo
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import utils.JsonObjectMapper
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class UndoTest {
    @Test
    fun Undoのシリアライズができる() {
        val undo = Undo(
            emptyList(),
            "Undo Follow",
            "https://follower.example.com/",
            "https://follower.example.com/undo/1",
            Follow(
                emptyList(),
                null,
                "https://follower.example.com/users/",
                actor = "https://follower.exaple.com/users/1"
            ),
            Instant.now(Clock.tickMillis(ZoneId.systemDefault()))
        )
        val writeValueAsString = JsonObjectMapper.objectMapper.writeValueAsString(undo)
        println(writeValueAsString)
    }

    @Test
    fun Undoをデシリアライズ出来る() {
        @Language("JSON")
        val json = """
          {
  "@context": [
    "https://www.w3.org/ns/activitystreams",
    "https://w3id.org/security/v1",
    {
      "manuallyApprovesFollowers": "as:manuallyApprovesFollowers",
      "sensitive": "as:sensitive",
      "Hashtag": "as:Hashtag",
      "quoteUrl": "as:quoteUrl",
      "toot": "http://joinmastodon.org/ns#",
      "Emoji": "toot:Emoji",
      "featured": "toot:featured",
      "discoverable": "toot:discoverable",
      "schema": "http://schema.org#",
      "PropertyValue": "schema:PropertyValue",
      "value": "schema:value",
      "misskey": "https://misskey-hub.net/ns#",
      "_misskey_content": "misskey:_misskey_content",
      "_misskey_quote": "misskey:_misskey_quote",
      "_misskey_reaction": "misskey:_misskey_reaction",
      "_misskey_votes": "misskey:_misskey_votes",
      "isCat": "misskey:isCat",
      "vcard": "http://www.w3.org/2006/vcard/ns#"
    }
  ],
  "type": "Undo",
  "id": "https://misskey.usbharu.dev/follows/97ws8y3rj6/9ezbh8qrh0/undo",
  "actor": "https://misskey.usbharu.dev/users/97ws8y3rj6",
  "object": {
    "id": "https://misskey.usbharu.dev/follows/97ws8y3rj6/9ezbh8qrh0",
    "type": "Follow",
    "actor": "https://misskey.usbharu.dev/users/97ws8y3rj6",
    "object": "https://test-hideout.usbharu.dev/users/test"
  },
  "published": "2023-05-20T10:28:17.308Z"
}
  
        """.trimIndent()

        val undo = JsonObjectMapper.objectMapper.readValue(json, Undo::class.java)
        println(undo)
    }
}
