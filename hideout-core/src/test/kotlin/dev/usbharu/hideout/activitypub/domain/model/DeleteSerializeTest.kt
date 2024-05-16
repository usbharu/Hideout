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

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.Constant
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
    "value" : "schema:value"
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
        expected.context = Constant.context
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
