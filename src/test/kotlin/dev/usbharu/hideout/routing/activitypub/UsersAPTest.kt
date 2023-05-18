package dev.usbharu.hideout.routing.activitypub

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.domain.model.ap.Image
import dev.usbharu.hideout.domain.model.ap.Key
import dev.usbharu.hideout.domain.model.ap.Person
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.plugins.configureSerialization
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.service.impl.IUserService
import dev.usbharu.hideout.util.HttpUtil.Activity
import dev.usbharu.hideout.util.HttpUtil.JsonLd
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

        val userService = mock<IUserService> {}

        val activityPubUserService = mock<ActivityPubUserService> {
            onBlocking { getPersonByName(anyString()) } doReturn person
        }

        application {
            configureSerialization()
            routing {
                usersAP(activityPubUserService, userService)
            }
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

    //    @Disabled
    @Test()
    fun `ユーザのURLにAcceptヘッダーをActivityとJson-LDにしてアクセスしたときPersonが返ってくる`() = testApplication {
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

        val userService = mock<IUserService> {}

        val activityPubUserService = mock<ActivityPubUserService> {
            onBlocking { getPersonByName(anyString()) } doReturn person
        }

        application {
            configureSerialization()
            routing {
                usersAP(activityPubUserService, userService)
            }
        }
        client.get("/users/test") {
            accept(ContentType.Application.JsonLd)
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

    //    @Disabled
    @Test
    fun contentType_Test() {
        assertTrue(ContentType.Application.Activity.match("application/activity+json"))
        val listOf = listOf(ContentType.Application.JsonLd, ContentType.Application.Activity)
        assertTrue(
            listOf.find { contentType ->
                contentType.match("application/ld+json; profile=\"https://www.w3.org/ns/activitystreams\"")
            }.let { it != null }
        )
        assertTrue(
            ContentType.Application.JsonLd.match(
                "application/ld+json; profile=\"https://www.w3.org/ns/activitystreams\""
            )
        )
    }

    @Test
    fun ユーザーのURLにAcceptヘッダーをhtmlにしてアクセスしたときはただの文字を返す() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val userService = mock<IUserService> {
            onBlocking { findByNameLocalUser(eq("test")) } doReturn User(
                1L,
                "test",
                "example.com",
                "test",
                "",
                "hashedPassword",
                "https://example.com/inbox",
                "https://example.com/outbox",
                "https://example.com",
                "-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----",
                "-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----",
                Instant.now()
            )
        }
        application {
            routing {
                usersAP(mock(), userService)
            }
        }
        client.get("/users/test") {
            accept(ContentType.Text.Html)
        }.let {
            assertEquals(HttpStatusCode.OK, it.status)
            assertTrue(it.contentType()?.match(ContentType.Text.Plain) == true)
        }
    }
}
