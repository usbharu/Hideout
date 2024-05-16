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
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class CreateTest {
    @Test
    fun Createのデイシリアライズができる() {
        @Language("JSON") val json = """{
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
  "id": "https://misskey.usbharu.dev/notes/9f2i9cm88e/activity",
  "actor": "https://misskey.usbharu.dev/users/97ws8y3rj6",
  "type": "Create",
  "published": "2023-05-22T14:26:53.600Z",
  "object": {
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
      {
        "type": "Mention",
        "href": "https://calckey.jp/users/9bu1xzwjyb",
        "name": "@trapezial@calckey.jp"
      }
    ]
  },
  "to": [
    "https://misskey.usbharu.dev/users/97ws8y3rj6/followers"
  ],
  "cc": [
    "https://www.w3.org/ns/activitystreams#Public",
    "https://calckey.jp/users/9bu1xzwjyb"
  ]
}
"""

        val objectMapper = ActivityPubConfig().objectMapper()

        objectMapper.readValue<Create>(json)
    }
}
