package dev.usbharu.hideout.routing.api.internal.v1

import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.Payload
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.domain.model.hideout.form.Post
import dev.usbharu.hideout.plugins.TOKEN_AUTH
import dev.usbharu.hideout.plugins.configureSerialization
import dev.usbharu.hideout.service.IPostService
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.Instant
import kotlin.test.assertEquals

class PostsKtTest {


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
        val postService = mock<IPostService> {
            onBlocking { create(any<PostCreateDto>()) } doAnswer {
                val argument = it.getArgument<PostCreateDto>(0)
                dev.usbharu.hideout.domain.model.hideout.entity.Post(
                    123L,
                    argument.userId,
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

        val post = Post("test")
        client.post("/api/internal/v1/posts") {
            header("Authorization", "Bearer asdkaf")
            contentType(ContentType.Application.Json)
            setBody(Config.configData.objectMapper.writeValueAsString(post))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("https://example.com", headers["Location"])
        }
        argumentCaptor<PostCreateDto> {
            verify(postService).create(capture())
            assertEquals(PostCreateDto("test", 1234), firstValue)
        }
    }
}
