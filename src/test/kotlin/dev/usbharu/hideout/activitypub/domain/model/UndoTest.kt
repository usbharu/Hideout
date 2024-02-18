/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.application.config.ActivityPubConfig
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class UndoTest {
    @Test
    fun Undoのシリアライズができる() {
        val undo = Undo(
            emptyList(),
            "https://follower.example.com/",
            "https://follower.example.com/undo/1",
            Follow(
                emptyList(),
                "https://follower.example.com/users/",
                actor = "https://follower.exaple.com/users/1"
            ),
            Instant.now(Clock.tickMillis(ZoneId.systemDefault())).toString()
        )
        val writeValueAsString = ActivityPubConfig().objectMapper().writeValueAsString(undo)
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

        val undo = ActivityPubConfig().objectMapper().readValue(json, Undo::class.java)
        println(undo)
    }

    @Test
    fun MastodonのUndoのデシリアライズができる() {
        //language=JSON
        val json = """{
  "@context" : "https://www.w3.org/ns/activitystreams",
  "id" : "https://kb.usbharu.dev/users/usbharu#follows/12/undo",
  "type" : "Undo",
  "actor" : "https://kb.usbharu.dev/users/usbharu",
  "object" : {
    "id" : "https://kb.usbharu.dev/0347b269-4dcb-4eb1-b8c4-b5f157bb6957",
    "type" : "Follow",
    "actor" : "https://kb.usbharu.dev/users/usbharu",
    "object" : "https://test-hideout.usbharu.dev/users/testuser15"
  }
}""".trimIndent()

        val undo = ActivityPubConfig().objectMapper().readValue<Undo>(json, Undo::class.java)

        println(undo)
    }
}
