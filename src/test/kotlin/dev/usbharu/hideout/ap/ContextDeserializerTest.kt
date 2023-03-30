package dev.usbharu.hideout.ap

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*

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
        val readValue = jacksonObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false).readValue<Follow>(s)
        println(readValue)
        println(readValue.actor)
    }
}
