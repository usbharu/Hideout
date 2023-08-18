package dev.usbharu.hideout.routing.api.internal.v1

import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.Payload
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.Acct
import dev.usbharu.hideout.domain.model.hideout.dto.UserResponse
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.hideout.form.UserCreate
import dev.usbharu.hideout.plugins.TOKEN_AUTH
import dev.usbharu.hideout.plugins.configureSecurity
import dev.usbharu.hideout.plugins.configureSerialization
import dev.usbharu.hideout.service.api.UserApiService
import dev.usbharu.hideout.service.user.UserService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import utils.JsonObjectMapper
import java.time.Instant
import kotlin.test.assertEquals
@Suppress("LargeClass")
class UsersTest {
    @Test
    fun `users にGETするとユーザー一覧を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }

        val users = listOf(
            UserResponse(
                "12345",
                "test1",
                "example.com",
                "test",
                "",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            ),
            UserResponse(
                "12343",
                "tes2",
                "example.com",
                "test",
                "",
                "https://example.com/tes2",
                Instant.now().toEpochMilli()
            ),
        )
        val userService = mock<UserApiService> {
            onBlocking { findAll(anyOrNull(), anyOrNull()) } doReturn users
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(mock(), userService)
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
        val userService = mock<UserService> {
            onBlocking { usernameAlreadyUse(any()) } doReturn false
            onBlocking { createLocalUser(any()) } doReturn User.of(
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
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(userService, mock())
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
        val userService = mock<UserService> {
            onBlocking { usernameAlreadyUse(any()) } doReturn true
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(userService, mock())
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

    @Test
    fun `users name にGETしたらユーザーを取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val userResponse = UserResponse(
            "1234",
            "test1",
            "example.com",
            "test",
            "test User",
            "https://example.com/test",
            Instant.now().toEpochMilli()
        )
        val userApiService = mock<UserApiService> {
            onBlocking { findByAcct(any()) } doReturn userResponse
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.get("/api/internal/v1/users/test1").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(userResponse, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users id にGETしたらユーザーを取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val userResponse = UserResponse(
            "1234",
            "test1",
            "example.com",
            "test",
            "test User",
            "https://example.com/test",
            Instant.now().toEpochMilli()
        )
        val userApiService = mock<UserApiService> {
            onBlocking { findById(any()) } doReturn userResponse
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.get("/api/internal/v1/users/1234").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(userResponse, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users name@domain にGETしたらユーザーを取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val userResponse = UserResponse(
            "1234",
            "test1",
            "example.com",
            "test",
            "test User",
            "https://example.com/test",
            Instant.now().toEpochMilli()
        )
        val userApiService = mock<UserApiService> {
            onBlocking { findByAcct(any()) } doReturn userResponse
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.get("/api/internal/v1/users/test1").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(userResponse, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users @name@domain にGETしたらユーザーを取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val userResponse = UserResponse(
            "1234",
            "test1",
            "example.com",
            "test",
            "test User",
            "https://example.com/test",
            Instant.now().toEpochMilli()
        )
        val userApiService = mock<UserApiService> {
            onBlocking { findByAcct(any()) } doReturn userResponse
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.get("/api/internal/v1/users/test1").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(userResponse, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users name followers にGETしたらフォロワー一覧を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }

        val followers = listOf(
            UserResponse(
                "1235",
                "follower1",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            ),
            UserResponse(
                "1236",
                "follower2",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            )
        )
        val userApiService = mock<UserApiService> {
            onBlocking { findFollowersByAcct(any()) } doReturn followers
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.get("/api/internal/v1/users/test1/followers").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(followers, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users name@domain followers にGETしたらフォロワー一覧を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }

        val followers = listOf(
            UserResponse(
                "1235",
                "follower1",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            ),
            UserResponse(
                "1236",
                "follower2",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            )
        )
        val userApiService = mock<UserApiService> {
            onBlocking { findFollowersByAcct(any()) } doReturn followers
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.get("/api/internal/v1/users/@test1@example.com/followers").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(followers, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users id followers にGETしたらフォロワー一覧を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }

        val followers = listOf(
            UserResponse(
                "1235",
                "follower1",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            ),
            UserResponse(
                "1236",
                "follower2",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            )
        )
        val userApiService = mock<UserApiService> {
            onBlocking { findFollowers(any()) } doReturn followers
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.get("/api/internal/v1/users/1234/followers").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(followers, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users name followers に認証情報ありでGETしたらフォローできる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }

        val claim = mock<Claim> {
            on { asLong() } doReturn 1234
        }
        val payload = mock<Payload> {
            on { getClaim(eq("uid")) } doReturn claim
        }

        val userApiService = mock<UserApiService> {
            onBlocking { findByAcct(any()) } doReturn UserResponse(
                "1235",
                "follower1",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            )
            onBlocking { follow(any<Acct>(), eq(1234)) } doReturn true
        }
        application {
            configureSerialization()
            authentication {
                bearer(TOKEN_AUTH) {
                    authenticate {
                        JWTPrincipal(payload)
                    }
                }
            }
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.post("/api/internal/v1/users/test1/followers") {
            header(HttpHeaders.Authorization, "Bearer test")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `users name followers に認証情報ありでGETしたらフォロー処理受付になることもある`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }

        val claim = mock<Claim> {
            on { asLong() } doReturn 1234
        }
        val payload = mock<Payload> {
            on { getClaim(eq("uid")) } doReturn claim
        }

        val userApiService = mock<UserApiService> {
            onBlocking { findByAcct(any()) } doReturn UserResponse(
                "1235",
                "follower1",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            )
            onBlocking { follow(any<Acct>(), eq(1234)) } doReturn false
        }
        application {
            configureSerialization()
            authentication {
                bearer(TOKEN_AUTH) {
                    authenticate {
                        JWTPrincipal(payload)
                    }
                }
            }
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.post("/api/internal/v1/users/test1/followers") {
            header(HttpHeaders.Authorization, "Bearer test")
        }.apply {
            assertEquals(HttpStatusCode.Accepted, status)
        }
    }

    @Test
    fun `users id followers に認証情報ありでGETしたらフォロー処理受付になることもある`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }

        val claim = mock<Claim> {
            on { asLong() } doReturn 1234
        }
        val payload = mock<Payload> {
            on { getClaim(eq("uid")) } doReturn claim
        }

        val userApiService = mock<UserApiService> {
            onBlocking { findById(any()) } doReturn UserResponse(
                "1235",
                "follower1",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            )
            onBlocking { follow(eq(1235), eq(1234)) } doReturn false
        }
        application {
            configureSerialization()
            authentication {
                bearer(TOKEN_AUTH) {
                    authenticate {
                        JWTPrincipal(payload)
                    }
                }
            }
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.post("/api/internal/v1/users/1235/followers") {
            header(HttpHeaders.Authorization, "Bearer test")
        }.apply {
            assertEquals(HttpStatusCode.Accepted, status)
        }
    }

    @Test
    fun `users name following にGETしたらフォロイー一覧を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }

        val followers = listOf(
            UserResponse(
                "1235",
                "follower1",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            ),
            UserResponse(
                "1236",
                "follower2",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            )
        )
        val userApiService = mock<UserApiService> {
            onBlocking { findFollowingsByAcct(any()) } doReturn followers
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.get("/api/internal/v1/users/test1/following").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(followers, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users name@domain following にGETしたらフォロイー一覧を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }

        val followers = listOf(
            UserResponse(
                "1235",
                "follower1",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            ),
            UserResponse(
                "1236",
                "follower2",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            )
        )
        val userApiService = mock<UserApiService> {
            onBlocking { findFollowingsByAcct(any()) } doReturn followers
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.get("/api/internal/v1/users/test1@domain/following").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(followers, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users id following にGETしたらフォロイー一覧を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }

        val followers = listOf(
            UserResponse(
                "1235",
                "follower1",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            ),
            UserResponse(
                "1236",
                "follower2",
                "example.com",
                "test",
                "test User",
                "https://example.com/test",
                Instant.now().toEpochMilli()
            )
        )
        val userApiService = mock<UserApiService> {
            onBlocking { findFollowings(any()) } doReturn followers
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                route("/api/internal/v1") {
                    users(mock(), userApiService)
                }
            }
        }

        client.get("/api/internal/v1/users/1234/following").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(followers, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }
}
