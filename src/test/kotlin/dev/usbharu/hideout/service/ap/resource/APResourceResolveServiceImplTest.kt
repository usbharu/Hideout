package dev.usbharu.hideout.service.ap.resource

import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.config.CharacterLimit
import dev.usbharu.hideout.domain.model.ap.Object
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.repository.UserRepository
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URL
import java.time.Instant
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
@Disabled
class APResourceResolveServiceImplTest {

    val userBuilder = User.UserBuilder(CharacterLimit(), ApplicationConfig(URL("https://example.com")))
    val postBuilder = Post.PostBuilder(CharacterLimit())

    @Test
    fun `単純な一回のリクエスト`() = runTest {

        var count = 0

        val httpClient = HttpClient(MockEngine { request ->
            count++
            respondOk("{}")
        })

        val userRepository = mock<UserRepository>()

        whenever(userRepository.findById(any())).doReturn(
            userBuilder.of(
                2L,
                "follower",
                "follower.example.com",
                "followerUser",
                "test follower user",
                "https://follower.example.com/inbox",
                "https://follower.example.com/outbox",
                "https://follower.example.com",
                "https://follower.example.com",
                publicKey = "",
                createdAt = Instant.now(),
                keyId = ""
            )
        )

        val apResourceResolveService =
            APResourceResolveServiceImpl(mock(), userRepository, InMemoryCacheManager())

        apResourceResolveService.resolve<Object>("https", 0)

        assertEquals(1, count)
    }

    @Test
    fun 複数回の同じリクエストが重複して発行されない() = runTest {
        var count = 0

        val httpClient = HttpClient(MockEngine { request ->
            count++
            respondOk("{}")
        })

        val userRepository = mock<UserRepository>()

        whenever(userRepository.findById(any())).doReturn(
            userBuilder.of(
                2L,
                "follower",
                "follower.example.com",
                "followerUser",
                "test follower user",
                "https://follower.example.com/inbox",
                "https://follower.example.com/outbox",
                "https://follower.example.com",
                "https://follower.example.com",
                publicKey = "",
                createdAt = Instant.now(),
                keyId = ""
            )
        )

        val apResourceResolveService =
            APResourceResolveServiceImpl(mock(), userRepository, InMemoryCacheManager())

        apResourceResolveService.resolve<Object>("https", 0)
        apResourceResolveService.resolve<Object>("https", 0)
        apResourceResolveService.resolve<Object>("https", 0)
        apResourceResolveService.resolve<Object>("https", 0)

        assertEquals(1, count)
    }

    @Test
    fun 複数回の同じリクエストが同時に発行されても重複して発行されない() = runTest {
        var count = 0

        val httpClient = HttpClient(MockEngine { request ->
            count++
            respondOk("{}")
        })

        val userRepository = mock<UserRepository>()

        whenever(userRepository.findById(any())).doReturn(
            userBuilder.of(
                2L,
                "follower",
                "follower.example.com",
                "followerUser",
                "test follower user",
                "https://follower.example.com/inbox",
                "https://follower.example.com/outbox",
                "https://follower.example.com",
                "https://follower.example.com",
                publicKey = "",
                createdAt = Instant.now(),
                keyId = ""
            )
        )

        val apResourceResolveService =
            APResourceResolveServiceImpl(mock(), userRepository, InMemoryCacheManager())

        repeat(10) {
            awaitAll(
                async { apResourceResolveService.resolve<Object>("https", 0) },
                async { apResourceResolveService.resolve<Object>("https", 0) },
                async { apResourceResolveService.resolve<Object>("https", 0) },
                async { apResourceResolveService.resolve<Object>("https", 0) },
                async { apResourceResolveService.resolve<Object>("https", 0) },
                async { apResourceResolveService.resolve<Object>("https", 0) },
                async { apResourceResolveService.resolve<Object>("https", 0) },
                async { apResourceResolveService.resolve<Object>("https", 0) },
                async { apResourceResolveService.resolve<Object>("https", 0) },
                async { apResourceResolveService.resolve<Object>("https", 0) },
                async { apResourceResolveService.resolve<Object>("https", 0) },
            )
        }

        assertEquals(1, count)
    }

    @Test
    fun 関係のないリクエストは発行する() = runTest {
        var count = 0

        val httpClient = HttpClient(MockEngine { request ->
            count++
            respondOk("{}")
        })

        val userRepository = mock<UserRepository>()

        whenever(userRepository.findById(any())).doReturn(
            userBuilder.of(
                2L,
                "follower",
                "follower.example.com",
                "followerUser",
                "test follower user",
                "https://follower.example.com/inbox",
                "https://follower.example.com/outbox",
                "https://follower.example.com",
                "https://follower.example.com",
                publicKey = "",
                createdAt = Instant.now(),
                keyId = ""
            )
        )

        val apResourceResolveService =
            APResourceResolveServiceImpl(mock(), userRepository, InMemoryCacheManager())

        apResourceResolveService.resolve<Object>("abcd", 0)
        apResourceResolveService.resolve<Object>("1234", 0)
        apResourceResolveService.resolve<Object>("aaaa", 0)

        assertEquals(3, count)
    }


}
