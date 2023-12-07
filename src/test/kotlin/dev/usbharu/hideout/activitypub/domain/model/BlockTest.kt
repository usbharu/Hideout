package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.application.config.ActivityPubConfig
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.springframework.boot.test.json.BasicJsonTester

class BlockTest {
    @Test
    fun blockDeserializeTest() {
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
  "type" : "Block",
  "id" : "https://misskey.usbharu.dev/blocks/9myf6e40vm",
  "actor" : "https://misskey.usbharu.dev/users/97ws8y3rj6",
  "object" : "https://test-hideout.usbharu.dev/users/test-user2"
}
"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val block = objectMapper.readValue<Block>(json)

        val expected = Block(
            "https://misskey.usbharu.dev/users/97ws8y3rj6",
            "https://misskey.usbharu.dev/blocks/9myf6e40vm",
            "https://test-hideout.usbharu.dev/users/test-user2"
        ).apply { context = listOf("https://www.w3.org/ns/activitystreams", "https://w3id.org/security/v1") }
        assertThat(block).isEqualTo(expected)
    }

    @Test
    fun blockSerializeTest() {
        val basicJsonTester = BasicJsonTester(javaClass)

        val block = Block(
            "https://misskey.usbharu.dev/users/97ws8y3rj6",
            "https://misskey.usbharu.dev/blocks/9myf6e40vm",
            "https://test-hideout.usbharu.dev/users/test-user2"
        ).apply { context = listOf("https://www.w3.org/ns/activitystreams", "https://w3id.org/security/v1") }

        val objectMapper = ActivityPubConfig().objectMapper()

        val writeValueAsString = objectMapper.writeValueAsString(block)

        val from = basicJsonTester.from(writeValueAsString)
        assertThat(from).extractingJsonPathStringValue("$.actor")
            .isEqualTo("https://misskey.usbharu.dev/users/97ws8y3rj6")
        assertThat(from).extractingJsonPathStringValue("$.id")
            .isEqualTo("https://misskey.usbharu.dev/blocks/9myf6e40vm")
        assertThat(from).extractingJsonPathStringValue("$.object")
            .isEqualTo("https://test-hideout.usbharu.dev/users/test-user2")

    }
}
