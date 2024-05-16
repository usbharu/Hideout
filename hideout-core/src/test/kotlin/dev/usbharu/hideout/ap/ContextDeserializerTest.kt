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

package dev.usbharu.hideout.ap

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Follow

class ContextDeserializerTest {

    @org.junit.jupiter.api.Test
    fun deserialize() {
        //language=JSON
        val s = """
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
      "_misskey_talk": "misskey:_misskey_talk",
      "isCat": "misskey:isCat",
      "vcard": "http://www.w3.org/2006/vcard/ns#"
    }
  ],
  "id": "https://test-misskey-v12.usbharu.dev/follows/9bg1zu54y7/9cydqvpjcn",
  "type": "Follow",
  "actor": "https://test-misskey-v12.usbharu.dev/users/9bg1zu54y7",
  "object": "https://test-hideout.usbharu.dev/users/test3"
}

"""
        val readValue = jacksonObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue<Follow>(s)
        println(readValue)
        println(readValue.actor)
    }
}
