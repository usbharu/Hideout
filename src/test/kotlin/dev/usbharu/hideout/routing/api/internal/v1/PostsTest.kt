package dev.usbharu.hideout.routing.api.internal.v1

import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.Payload
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.plugins.TOKEN_AUTH
import dev.usbharu.hideout.plugins.configureSecurity
import dev.usbharu.hideout.plugins.configureSerialization
import dev.usbharu.hideout.service.api.IPostApiService
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
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class PostsTest {

    @Test
    fun 認証情報無しでpostsにGETしたらPUBLICな投稿一覧が返ってくる() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val posts = listOf(
            Post(
                id = 12345,
                userId = 4321,
                text = "test1",
                visibility = Visibility.PUBLIC,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/1"
            ),
            Post(
                id = 123456,
                userId = 4322,
                text = "test2",
                visibility = Visibility.PUBLIC,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/2"
            )
        )
        val postService = mock<IPostApiService> {
            onBlocking {
                getAll(
                    since = anyOrNull(),
                    until = anyOrNull(),
                    minId = anyOrNull(),
                    maxId = anyOrNull(),
                    limit = anyOrNull(),
                    userId = isNull()
                )
            } doReturn posts
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    posts(postService)
                }
            }
        }

        client.get("/api/internal/v1/posts").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertContentEquals(posts, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun 認証情報ありでpostsにGETすると権限のある投稿が返ってくる() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val claim = mock<Claim> {
            on { asLong() } doReturn 1234
        }
        val payload = mock<Payload> {
            on { getClaim(eq("uid")) } doReturn claim
        }

        val posts = listOf(
            Post(
                id = 12345,
                userId = 4321,
                text = "test1",
                visibility = Visibility.PUBLIC,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/1"
            ),
            Post(
                id = 123456,
                userId = 4322,
                text = "test2",
                visibility = Visibility.PUBLIC,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/2"
            ),
            Post(
                id = 1234567,
                userId = 4333,
                text = "Followers only",
                visibility = Visibility.FOLLOWERS,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/3"
            )
        )

        val postService = mock<IPostApiService> {
            onBlocking {
                getAll(
                    since = anyOrNull(),
                    until = anyOrNull(),
                    minId = anyOrNull(),
                    maxId = anyOrNull(),
                    limit = anyOrNull(),
                    userId = isNotNull()
                )
            } doReturn posts
        }
        application {
            authentication {
                bearer(TOKEN_AUTH) {
                    authenticate {
                        JWTPrincipal(payload)
                    }
                }
            }
            configureSerialization()
            routing {
                route("/api/internal/v1") {
                    posts(postService)
                }
            }
        }
        client.get("/api/internal/v1/posts") {
            header("Authorization", "Bearer asdkaf")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `posts id にGETしたらPUBLICな投稿を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val post = Post(
            12345,
            1234,
            text = "aaa",
            visibility = Visibility.PUBLIC,
            createdAt = Instant.now().toEpochMilli(),
            url = "https://example.com/posts/1"
        )
        val postService = mock<IPostApiService> {
            onBlocking { getById(any(), anyOrNull()) } doReturn post
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    posts(postService)
                }
            }
        }
        client.get("/api/internal/v1/posts/1").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(post, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `認証情報ありでposts id にGETしたら権限のある投稿を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val post = Post(
            12345,
            1234,
            text = "aaa",
            visibility = Visibility.FOLLOWERS,
            createdAt = Instant.now().toEpochMilli(),
            url = "https://example.com/posts/1"
        )
        val postService = mock<IPostApiService> {
            onBlocking { getById(any(), isNotNull()) } doReturn post
        }
        val claim = mock<Claim> {
            on { asLong() } doReturn 1234
        }
        val payload = mock<Payload> {
            on { getClaim(eq("uid")) } doReturn claim
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
                    posts(postService)
                }
            }
        }
        client.get("/api/internal/v1/posts/1") {
            header("Authorization", "Bearer asdkaf")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(post, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `posts-post postsにpostしたら投稿できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val claim = mock<Claim> {
            on { asLong() } doReturn 1234
        }
        val payload = mock<Payload> {
            on { getClaim(eq("uid")) } doReturn claim
        }
        val postService = mock<IPostApiService> {
            onBlocking { createPost(any(), any()) } doAnswer {
                val argument = it.getArgument<dev.usbharu.hideout.domain.model.hideout.form.Post>(0)
                val userId = it.getArgument<Long>(1)
                Post(
                    123L,
                    userId,
                    null,
                    argument.text,
                    Instant.now().toEpochMilli(),
                    Visibility.PUBLIC,
                    "https://example.com"
                )
            }
        }
        application {
            authentication {
                bearer(TOKEN_AUTH) {
                    authenticate {
                        println("aaaaaaaaaaaa")
                        JWTPrincipal(payload)
                    }
                }
            }
            routing {
                route("/api/internal/v1") {
                    posts(postService)
                }
            }
            configureSerialization()
        }

        val post = dev.usbharu.hideout.domain.model.hideout.form.Post("test")
        client.post("/api/internal/v1/posts") {
            header("Authorization", "Bearer asdkaf")
            contentType(ContentType.Application.Json)
            setBody(Config.configData.objectMapper.writeValueAsString(post))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("https://example.com", headers["Location"])
        }
        argumentCaptor<dev.usbharu.hideout.domain.model.hideout.form.Post> {
            verify(postService).createPost(capture(), any())
            assertEquals(dev.usbharu.hideout.domain.model.hideout.form.Post("test"), firstValue)
        }
    }

    @Test
    fun `users userId postsにGETしたらユーザーのPUBLICな投稿一覧を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val posts = listOf(
            Post(
                id = 12345,
                userId = 1,
                text = "test1",
                visibility = Visibility.PUBLIC,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/1"
            ),
            Post(
                id = 123456,
                userId = 1,
                text = "test2",
                visibility = Visibility.PUBLIC,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/2"
            )
        )
        val postService = mock<IPostApiService> {
            onBlocking {
                getByUser(
                    nameOrId = any(),
                    since = anyOrNull(),
                    until = anyOrNull(),
                    minId = anyOrNull(),
                    maxId = anyOrNull(),
                    limit = anyOrNull(),
                    userId = anyOrNull()
                )
            } doReturn posts
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    posts(postService)
                }
            }
        }

        client.get("/api/internal/v1/users/1/posts").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(posts, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users username postsにGETしたらユーザーのPUBLICな投稿一覧を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val posts = listOf(
            Post(
                id = 12345,
                userId = 1,
                text = "test1",
                visibility = Visibility.PUBLIC,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/1"
            ),
            Post(
                id = 123456,
                userId = 1,
                text = "test2",
                visibility = Visibility.PUBLIC,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/2"
            )
        )
        val postService = mock<IPostApiService> {
            onBlocking {
                getByUser(
                    nameOrId = eq("test1"),
                    since = anyOrNull(),
                    until = anyOrNull(),
                    minId = anyOrNull(),
                    maxId = anyOrNull(),
                    limit = anyOrNull(),
                    userId = anyOrNull()
                )
            } doReturn posts
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    posts(postService)
                }
            }
        }

        client.get("/api/internal/v1/users/test1/posts").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(posts, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users username@domain postsにGETしたらユーザーのPUBLICな投稿一覧を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val posts = listOf(
            Post(
                id = 12345,
                userId = 1,
                text = "test1",
                visibility = Visibility.PUBLIC,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/1"
            ),
            Post(
                id = 123456,
                userId = 1,
                text = "test2",
                visibility = Visibility.PUBLIC,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/2"
            )
        )
        val postService = mock<IPostApiService> {
            onBlocking {
                getByUser(
                    nameOrId = eq("test1@example.com"),
                    since = anyOrNull(),
                    until = anyOrNull(),
                    minId = anyOrNull(),
                    maxId = anyOrNull(),
                    limit = anyOrNull(),
                    userId = anyOrNull()
                )
            } doReturn posts
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    posts(postService)
                }
            }
        }

        client.get("/api/internal/v1/users/test1@example.com/posts").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(posts, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users @username@domain postsにGETしたらユーザーのPUBLICな投稿一覧を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val posts = listOf(
            Post(
                id = 12345,
                userId = 1,
                text = "test1",
                visibility = Visibility.PUBLIC,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/1"
            ),
            Post(
                id = 123456,
                userId = 1,
                text = "test2",
                visibility = Visibility.PUBLIC,
                createdAt = Instant.now().toEpochMilli(),
                url = "https://example.com/posts/2"
            )
        )
        val postService = mock<IPostApiService> {
            onBlocking {
                getByUser(
                    nameOrId = eq("@test1@example.com"),
                    since = anyOrNull(),
                    until = anyOrNull(),
                    minId = anyOrNull(),
                    maxId = anyOrNull(),
                    limit = anyOrNull(),
                    userId = anyOrNull()
                )
            } doReturn posts
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    posts(postService)
                }
            }
        }

        client.get("/api/internal/v1/users/@test1@example.com/posts").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(posts, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users name posts id にGETしたらPUBLICな投稿を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val post = Post(
            id = 123456,
            userId = 1,
            text = "test2",
            visibility = Visibility.PUBLIC,
            createdAt = Instant.now().toEpochMilli(),
            url = "https://example.com/posts/2"
        )
        val postService = mock<IPostApiService> {
            onBlocking { getById(eq(12345L), anyOrNull()) } doReturn post
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    posts(postService)
                }
            }
        }

        client.get("/api/internal/v1/users/test/posts/12345").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(post, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users id posts id にGETしたらPUBLICな投稿を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val post = Post(
            id = 123456,
            userId = 1,
            text = "test2",
            visibility = Visibility.PUBLIC,
            createdAt = Instant.now().toEpochMilli(),
            url = "https://example.com/posts/2"
        )
        val postService = mock<IPostApiService> {
            onBlocking { getById(eq(12345L), anyOrNull()) } doReturn post
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    posts(postService)
                }
            }
        }

        client.get("/api/internal/v1/users/1/posts/12345").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(post, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users name posts id にGETしたらUserIdが間違っててもPUBLICな投稿を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val post = Post(
            id = 123456,
            userId = 1,
            text = "test2",
            visibility = Visibility.PUBLIC,
            createdAt = Instant.now().toEpochMilli(),
            url = "https://example.com/posts/2"
        )
        val postService = mock<IPostApiService> {
            onBlocking { getById(eq(12345L), anyOrNull()) } doReturn post
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    posts(postService)
                }
            }
        }

        client.get("/api/internal/v1/users/423827849732847/posts/12345").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(post, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }

    @Test
    fun `users name posts id にGETしたらuserNameが間違っててもPUBLICな投稿を取得できる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val post = Post(
            id = 123456,
            userId = 1,
            text = "test2",
            visibility = Visibility.PUBLIC,
            createdAt = Instant.now().toEpochMilli(),
            url = "https://example.com/posts/2"
        )
        val postService = mock<IPostApiService> {
            onBlocking { getById(eq(12345L), anyOrNull()) } doReturn post
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock(), mock(), mock(), mock())
            routing {
                route("/api/internal/v1") {
                    posts(postService)
                }
            }
        }

        client.get("/api/internal/v1/users/invalidUserName/posts/12345").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(post, JsonObjectMapper.objectMapper.readValue(bodyAsText()))
        }
    }
}
