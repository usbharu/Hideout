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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonLdSerializeTest {
    @Test
    fun contextが文字列のときデシリアライズできる() {
        //language=JSON
        val json = """{"@context":"https://example.com"}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<JsonLd>(json)

        assertEquals(JsonLd(listOf(StringOrObject("https://example.com"))), readValue)
    }

    @Test
    fun contextが文字列の配列のときデシリアライズできる() {
        //language=JSON
        val json = """{"@context":["https://example.com","https://www.w3.org/ns/activitystreams"]}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<JsonLd>(json)

        assertEquals(
            JsonLd(
                listOf(
                    StringOrObject("https://example.com"),
                    StringOrObject("https://www.w3.org/ns/activitystreams")
                )
            ), readValue
        )
    }

    @Test
    fun contextがnullのとき空のlistとして解釈してデシリアライズする() {
        //language=JSON
        val json = """{"@context":null}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<JsonLd>(json)

        assertEquals(JsonLd(emptyList()), readValue)
    }

    @Test
    fun contextがnullを含む文字列の配列のときnullを無視してデシリアライズできる() {
        //language=JSON
        val json = """{"@context":["https://example.com",null,"https://www.w3.org/ns/activitystreams"]}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<JsonLd>(json)

        assertEquals(
            JsonLd(
                listOf(
                    StringOrObject("https://example.com"),
                    StringOrObject("https://www.w3.org/ns/activitystreams")
                )
            ), readValue
        )
    }

    @Test
    fun contextがオブジェクトのとき無視してデシリアライズする() {
        //language=JSON
        val json = """{"@context":{"hoge": "fuga"}}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<JsonLd>(json)

        assertEquals(JsonLd(listOf(StringOrObject(mapOf("hoge" to "fuga")))), readValue)
    }

    @Test
    fun contextがオブジェクトを含む文字列の配列のときオブジェクトを無視してデシリアライズする() {
        //language=JSON
        val json = """{"@context":["https://example.com",{"hoge": "fuga"},"https://www.w3.org/ns/activitystreams"]}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<JsonLd>(json)

        assertEquals(
            JsonLd(
                listOf(
                    StringOrObject("https://example.com"),
                    StringOrObject(mapOf("hoge" to "fuga")),
                    StringOrObject("https://www.w3.org/ns/activitystreams")
                )
            ), readValue
        )
    }

    @Test
    fun contextが配列の配列のとき無視してデシリアライズする() {
        //language=JSON
        val json = """{"@context":[["a","b"],["c","d"]]}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<JsonLd>(json)

        assertEquals(JsonLd(emptyList()), readValue)
    }

    @Test
    fun contextが空のとき無視してシリアライズする() {
        val jsonLd = JsonLd(emptyList())

        val objectMapper = ActivityPubConfig().objectMapper()

        val actual = objectMapper.writeValueAsString(jsonLd)

        assertEquals("{}", actual)
    }

    @Test
    fun contextがnullのとき無視してシリアライズする() {
        val jsonLd = JsonLd(listOf(null))

        val objectMapper = ActivityPubConfig().objectMapper()

        val actual = objectMapper.writeValueAsString(jsonLd)

        assertEquals("{}", actual)
    }

    @Test
    fun contextが文字列のとき文字列としてシリアライズされる() {
        val jsonLd = JsonLd(listOf(StringOrObject("https://example.com")))

        val objectMapper = ActivityPubConfig().objectMapper()

        val actual = objectMapper.writeValueAsString(jsonLd)

        assertEquals("""{"@context":"https://example.com"}""", actual)
    }

    @Test
    fun contextが文字列の配列のとき配列としてシリアライズされる() {
        val jsonLd = JsonLd(
            listOf(
                StringOrObject("https://example.com"),
                StringOrObject("https://www.w3.org/ns/activitystreams")
            )
        )

        val objectMapper = ActivityPubConfig().objectMapper()

        val actual = objectMapper.writeValueAsString(jsonLd)

        assertEquals("""{"@context":["https://example.com","https://www.w3.org/ns/activitystreams"]}""", actual)
    }

    @Test
    fun contextがオブジェクトのときシリアライズできる() {
        val jsonLd = JsonLd(
            listOf(
                StringOrObject(mapOf("hoge" to "fuga"))
            )
        )

        val objectMapper = ActivityPubConfig().objectMapper()

        val actual = objectMapper.writeValueAsString(jsonLd)

        assertEquals("""{"@context":{"hoge":"fuga"}}""", actual)

    }

    @Test
    fun contextが複数のオブジェクトのときシリアライズできる() {
        val jsonLd = JsonLd(
            listOf(
                StringOrObject(mapOf("hoge" to "fuga")),
                StringOrObject(mapOf("foo" to "bar"))
            )
        )

        val objectMapper = ActivityPubConfig().objectMapper()

        val actual = objectMapper.writeValueAsString(jsonLd)

        assertEquals("""{"@context":[{"hoge":"fuga"},{"foo":"bar"}]}""", actual)
    }

    @Test
    fun contextが複数のオブジェクトのときデシリアライズできる() {
        //language=JSON
        val json = """{"@context":["https://example.com",{"hoge": "fuga"},{"foo": "bar"}]}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<JsonLd>(json)

        assertEquals(
            JsonLd(
                listOf(
                    StringOrObject("https://example.com"),
                    StringOrObject(mapOf("hoge" to "fuga")),
                    StringOrObject(mapOf("foo" to "bar"))
                )
            ), readValue
        )
    }

    @Test
    fun contextがオブジェクトのときデシリアライズできる() {
        //language=JSON
        val json = """{"@context":{"hoge": "fuga"}}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<JsonLd>(json)

        assertEquals(
            JsonLd(
                listOf(
                    StringOrObject(mapOf("hoge" to "fuga"))
                )
            ), readValue
        )
    }
}
