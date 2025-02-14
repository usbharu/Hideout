package dev.usbharu.hideout.activitypub.external.activitystreams

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jsonldjava.core.JsonLdOptions
import com.github.jsonldjava.core.JsonLdProcessor
import com.github.jsonldjava.utils.JsonUtils
import dev.usbharu.activitystreamsserialization.json.impl.JacksonSerializationConverter
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActorTranslatorTest {
    @Test
    fun translate() {
        val actor = TestActorFactory.create()
        val translate = ActorTranslator().translate(actor, null, null)
        println(translate)
        val compact = JsonLdProcessor.compact(
            JsonUtils.fromString(JacksonSerializationConverter.convert(translate.json).toString()),
            "https://www.w3.org/ns/activitystreams",
            JsonLdOptions()
        )
        println(JsonUtils.toPrettyString(compact))

        val readTree = jacksonObjectMapper().readTree(JsonUtils.toString(compact))

        assertEquals(actor.url.toString(), readTree["id"].asText())

        assertEquals("Person", readTree["type"].asText())

        // inbox, outbox のテスト
        assertEquals(actor.inbox.toString(), readTree["inbox"].asText())
        assertEquals(actor.outbox.toString(), readTree["outbox"].asText())

        // followers, following のテスト
        assertEquals(actor.followersEndpoint.toString(), readTree["followers"].asText())
        assertEquals(actor.followingEndpoint.toString(), readTree["following"].asText())

        // preferredUsername のテスト
        assertEquals(actor.name.name, readTree["preferredUsername"].asText())

        // name のテスト
        assertEquals(actor.screenName.screenName, readTree["name"].asText())

        // publicKey のテスト
        val publicKeyNode = readTree["https://w3id.org/security#publicKey"]
        assertTrue(publicKeyNode.isObject)  // publicKey がオブジェクトか確認
        assertEquals(actor.keyId.keyId, publicKeyNode["id"].asText())
        assertEquals(actor.url.toString(), publicKeyNode["https://w3id.org/security#owner"].asText())
        assertEquals(actor.publicKey.publicKey, publicKeyNode["https://w3id.org/security#publicKeyPem"].asText())

        // @context のテスト
        assertEquals("https://www.w3.org/ns/activitystreams", readTree["@context"].asText())
    }
}