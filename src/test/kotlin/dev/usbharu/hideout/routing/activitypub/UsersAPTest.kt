package dev.usbharu.hideout.routing.activitypub

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.ap.Image
import dev.usbharu.hideout.ap.Key
import dev.usbharu.hideout.ap.Person
import dev.usbharu.hideout.plugins.configureRouting
import dev.usbharu.hideout.plugins.configureSerialization
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.service.impl.UserService
import dev.usbharu.hideout.service.signature.HttpSignatureVerifyService
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.assertEquals


class UsersAPTest {

    @Test()
    fun `ユーザのURLにAcceptヘッダーをActivityにしてアクセスしたときPersonが返ってくる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val person = Person(
            type = emptyList(),
            name = "test",
            id = "http://example.com/users/test",
            preferredUsername = "test",
            summary = "test user",
            inbox = "http://example.com/users/test/inbox",
            outbox = "http://example.com/users/test/outbox",
            url = "http://example.com/users/test",
            icon = Image(
                type = emptyList(),
                name = "http://example.com/users/test/icon.png",
                mediaType = "image/png",
                url = "http://example.com/users/test/icon.png"
            ),
            publicKey = Key(
                type = emptyList(),
                name = "Public Key",
                id = "http://example.com/users/test#pubkey",
                owner = "https://example.com/users/test",
                publicKeyPem = "-----BEGIN PUBLIC KEY-----\n\n-----END PUBLIC KEY-----"
            )
        )
        person.context = listOf("https://www.w3.org/ns/activitystreams")

        val httpSignatureVerifyService = mock<HttpSignatureVerifyService> {}
        val activityPubService = mock<ActivityPubService> {}
        val userService = mock<UserService> {}

        val activityPubUserService = mock<ActivityPubUserService> {
            onBlocking { getPersonByName(anyString()) } doReturn person
        }

        application {
            configureSerialization()
            configureRouting(httpSignatureVerifyService, activityPubService, userService, activityPubUserService)
        }
        client.get("/users/test") {
            accept(ContentType.Application.Activity)
        }.let {
            val objectMapper = jacksonObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            objectMapper.configOverride(List::class.java).setSetterInfo(
                JsonSetter.Value.forValueNulls(
                    Nulls.AS_EMPTY
                )
            )
            val actual = it.bodyAsText()
            val readValue = objectMapper.readValue<Person>(actual)
            assertEquals(person, readValue)
        }
    }
}
