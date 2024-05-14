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
import dev.usbharu.hideout.application.config.ActivityPubConfig
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.springframework.boot.test.json.BasicJsonTester

class RejectTest {
    @Test
    fun rejectDeserializeTest() {
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
    "_misskey_summary" : "misskey:_misskey_summary",
    "isCat" : "misskey:isCat",
    "vcard" : "http://www.w3.org/2006/vcard/ns#"
  } ],
  "type" : "Reject",
  "actor" : "https://misskey.usbharu.dev/users/97ws8y3rj6",
  "object" : {
    "id" : "https://misskey.usbharu.dev/follows/9mxh6mawru/97ws8y3rj6",
    "type" : "Follow",
    "actor" : "https://test-hideout.usbharu.dev/users/test-user2",
    "object" : "https://misskey.usbharu.dev/users/97ws8y3rj6"
  },
  "id" : "https://misskey.usbharu.dev/06407419-5aeb-4e2d-8885-aa54b03decf0"
}
"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val reject = objectMapper.readValue<Reject>(json)

        val expected = Reject(
            "https://misskey.usbharu.dev/users/97ws8y3rj6",
            "https://misskey.usbharu.dev/06407419-5aeb-4e2d-8885-aa54b03decf0",
            Follow(
                apObject = "https://misskey.usbharu.dev/users/97ws8y3rj6",
                actor = "https://test-hideout.usbharu.dev/users/test-user2",
                id = "https://misskey.usbharu.dev/follows/9mxh6mawru/97ws8y3rj6"
            )
        ).apply {
            context = listOf(
                StringOrObject("https://www.w3.org/ns/activitystreams"),
                StringOrObject("https://w3id.org/security/v1")
            )
        }

        assertThat(reject).isEqualTo(expected)
    }

    @Test
    fun rejectSerializeTest() {
        val basicJsonTester = BasicJsonTester(javaClass)

        val reject = Reject(
            "https://misskey.usbharu.dev/users/97ws8y3rj6",
            "https://misskey.usbharu.dev/06407419-5aeb-4e2d-8885-aa54b03decf0",
            Follow(
                apObject = "https://misskey.usbharu.dev/users/97ws8y3rj6",
                actor = "https://test-hideout.usbharu.dev/users/test-user2"
            )
        ).apply {
            context = listOf(
                StringOrObject("https://www.w3.org/ns/activitystreams"),
                StringOrObject("https://w3id.org/security/v1")
            )
        }

        val objectMapper = ActivityPubConfig().objectMapper()

        val writeValueAsString = objectMapper.writeValueAsString(reject)

        val from = basicJsonTester.from(writeValueAsString)

        assertThat(from).extractingJsonPathStringValue("$.actor")
            .isEqualTo("https://misskey.usbharu.dev/users/97ws8y3rj6")
        assertThat(from).extractingJsonPathStringValue("$.id")
            .isEqualTo("https://misskey.usbharu.dev/06407419-5aeb-4e2d-8885-aa54b03decf0")
        assertThat(from).extractingJsonPathStringValue("$.type").isEqualTo("Reject")
        assertThat(from).extractingJsonPathStringValue("$.object.actor")
            .isEqualTo("https://test-hideout.usbharu.dev/users/test-user2")
        assertThat(from).extractingJsonPathStringValue("$.object.object")
            .isEqualTo("https://misskey.usbharu.dev/users/97ws8y3rj6")
        assertThat(from).extractingJsonPathStringValue("$.object.type").isEqualTo("Follow")
    }
}
