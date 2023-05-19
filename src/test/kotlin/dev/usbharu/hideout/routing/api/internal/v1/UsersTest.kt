package dev.usbharu.hideout.routing.api.internal.v1

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.UserResponse
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.hideout.form.UserCreate
import dev.usbharu.hideout.plugins.configureSecurity
import dev.usbharu.hideout.plugins.configureSerialization
import dev.usbharu.hideout.service.impl.IUserService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import utils.JsonObjectMapper
import java.time.Instant
import kotlin.test.assertEquals

class UsersTest {
    @Test
    fun `users にGETするとユーザー一覧を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }

        val users = listOf(
            UserResponse(
                12345,
                "test1",
                "example.com",
                "test",
                "",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            ),
            UserResponse(
                12343,
                "tes2",
                "example.com",
                "test",
                "",
                "https://example.com/tes2",
                Instant.now().toEpochMilli()
            ),
        )
        val userService = mock<IUserService> {
            onBlocking { findAllForUser(anyOrNull(), anyOrNull()) } doReturn users
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(userService)
                }
            }
        }
        client.get("/api/internal/v1/users").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(users, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users にPOSTすると新規ユーザー作成ができる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val userCreateDto = UserCreate("test", "XXXXXXX")
        val userService = mock<IUserService> {
            onBlocking { usernameAlreadyUse(any()) } doReturn false
            onBlocking { createLocalUser(any()) } doReturn User(
                id = 12345,
                name = "test",
                domain = "example.com",
                screenName = "testUser",
                description = "test user",
                password = "XXXXXXX",
                inbox = "https://example.com/inbox",
                outbox = "https://example.com/outbox",
                url = "https://example.com",
                publicKey = "-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----",
                privateKey = "-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----",
                createdAt = Instant.now()
            )
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(userService)
                }
            }
        }

        client.post("/api/internal/v1/users") {
            contentType(ContentType.Application.Json)
            setBody(JsonObjectMapper.objectMapper.writeValueAsString(userCreateDto))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            assertEquals(
                "${Config.configData.url}/api/internal/v1/users/${userCreateDto.username}",
                headers["Location"]
            )
        }
    }

    @Test
    fun `users 既にユーザー名が使用されているときはBadRequestが帰ってくる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val userCreateDto = UserCreate("test", "XXXXXXX")
        val userService = mock<IUserService> {
            onBlocking { usernameAlreadyUse(any()) } doReturn true
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(userService)
                }
            }
        }

        client.post("/api/internal/v1/users") {
            contentType(ContentType.Application.Json)
            setBody(JsonObjectMapper.objectMapper.writeValueAsString(userCreateDto))
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }
}
