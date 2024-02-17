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
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteServiceImpl.Companion.public
import dev.usbharu.hideout.application.config.ActivityPubConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NoteSerializeTest {
    @Test
    fun Noteのシリアライズができる() {
        val note = Note(
            id = "https://example.com",
            attributedTo = "https://example.com/actor",
            content = "Hello",
            published = "2023-05-20T10:28:17.308Z",
        )

        val objectMapper = ActivityPubConfig().objectMapper()

        val writeValueAsString = objectMapper.writeValueAsString(note)

        assertEquals(
            """{"type":"Note","id":"https://example.com","attributedTo":"https://example.com/actor","content":"Hello","published":"2023-05-20T10:28:17.308Z","sensitive":false}""",
            writeValueAsString
        )
    }

    @Test
    fun Noteのデシリアライズができる() {
        //language=JSON
        val json = """{
    "id": "https://misskey.usbharu.dev/notes/9f2i9cm88e",
    "type": "Note",
    "attributedTo": "https://misskey.usbharu.dev/users/97ws8y3rj6",
    "content": "<p><a href=\"https://calckey.jp/@trapezial\" class=\"u-url mention\">@trapezial@calckey.jp</a><span> いやそういうことじゃなくて、連合先と自インスタンスで状態が狂うことが多いのでどっちに合わせるべきかと…</span></p>",
    "_misskey_content": "@trapezial@calckey.jp いやそういうことじゃなくて、連合先と自インスタンスで状態が狂うことが多いのでどっちに合わせるべきかと…",
    "source": {
      "content": "@trapezial@calckey.jp いやそういうことじゃなくて、連合先と自インスタンスで状態が狂うことが多いのでどっちに合わせるべきかと…",
      "mediaType": "text/x.misskeymarkdown"
    },
    "published": "2023-05-22T14:26:53.600Z",
    "to": [
      "https://misskey.usbharu.dev/users/97ws8y3rj6/followers"
    ],
    "cc": [
      "https://www.w3.org/ns/activitystreams#Public",
      "https://calckey.jp/users/9bu1xzwjyb"
    ],
    "inReplyTo": "https://calckey.jp/notes/9f2i7ymf1d",
    "attachment": [],
    "sensitive": false,
    "tag": [
      
    ]
  }"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<Note>(json)

        val note = Note(
            id = "https://misskey.usbharu.dev/notes/9f2i9cm88e",
            type = listOf("Note"),
            attributedTo = "https://misskey.usbharu.dev/users/97ws8y3rj6",
            content = "<p><a href=\"https://calckey.jp/@trapezial\" class=\"u-url mention\">@trapezial@calckey.jp</a><span> いやそういうことじゃなくて、連合先と自インスタンスで状態が狂うことが多いのでどっちに合わせるべきかと…</span></p>",
            published = "2023-05-22T14:26:53.600Z",
            to = listOf("https://misskey.usbharu.dev/users/97ws8y3rj6/followers"),
            cc = listOf(public, "https://calckey.jp/users/9bu1xzwjyb"),
            sensitive = false,
            inReplyTo = "https://calckey.jp/notes/9f2i7ymf1d",
            attachment = emptyList()
        )
        assertEquals(note, readValue)
    }

    @Test
    fun 絵文字付きNoteのデシリアライズができる() {
        val json = """{
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
      "_misskey_summary": "misskey:_misskey_summary",
      "isCat": "misskey:isCat",
      "vcard": "http://www.w3.org/2006/vcard/ns#"
    }
  ],
  "id": "https://misskey.usbharu.dev/notes/9nj1omt1rn",
  "type": "Note",
  "attributedTo": "https://misskey.usbharu.dev/users/97ws8y3rj6",
  "content": "<p>​:oyasumi:​</p>",
  "_misskey_content": ":oyasumi:",
  "source": {
    "content": ":oyasumi:",
    "mediaType": "text/x.misskeymarkdown"
  },
  "published": "2023-12-21T17:32:36.853Z",
  "to": [
    "https://www.w3.org/ns/activitystreams#Public"
  ],
  "cc": [
    "https://misskey.usbharu.dev/users/97ws8y3rj6/followers"
  ],
  "inReplyTo": null,
  "attachment": [],
  "sensitive": false,
  "tag": [
    {
      "id": "https://misskey.usbharu.dev/emojis/oyasumi",
      "type": "Emoji",
      "name": ":oyasumi:",
      "updated": "2023-04-07T08:21:25.246Z",
      "icon": {
        "type": "Image",
        "mediaType": "image/png",
        "url": "https://s3misskey.usbharu.dev/misskey-minio/misskey-minio/data/cf8db710-1d70-4076-8a00-dbb28131096e.png"
      }
    }
  ]
}"""


        val objectMapper = ActivityPubConfig().objectMapper()

        val expected = Note(
            type = emptyList(),
            id = "https://misskey.usbharu.dev/notes/9nj1omt1rn",
            attributedTo = "https://misskey.usbharu.dev/users/97ws8y3rj6",
            content = "<p>\u200B:oyasumi:\u200B</p>",
            published = "2023-12-21T17:32:36.853Z",
            to = listOf("https://www.w3.org/ns/activitystreams#Public"),
            cc = listOf("https://misskey.usbharu.dev/users/97ws8y3rj6/followers"),
            sensitive = false,
            inReplyTo = null,
            attachment = emptyList(),
            tag = listOf(
                Emoji(
                    type = emptyList(),
                    name = ":oyasumi:",
                    id = "https://misskey.usbharu.dev/emojis/oyasumi",
                    updated = "2023-04-07T08:21:25.246Z",
                    icon = Image(
                        type = emptyList(),
                        mediaType = "image/png",
                        url = "https://s3misskey.usbharu.dev/misskey-minio/misskey-minio/data/cf8db710-1d70-4076-8a00-dbb28131096e.png"
                    )
                )
            )
        )

        expected.context = listOf(
            "https://www.w3.org/ns/activitystreams",
            "https://w3id.org/security/v1"
        )

        val note = objectMapper.readValue<Note>(json)

        assertThat(note).isEqualTo(expected)
    }
}
