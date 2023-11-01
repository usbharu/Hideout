package dev.usbharu.hideout.service.ap.resource

import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.config.CharacterLimit
import dev.usbharu.hideout.domain.model.ap.Object
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.repository.UserRepository
import dev.usbharu.hideout.service.ap.APRequestService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.UserBuilder
import java.net.URL

@ExtendWith(MockitoExtension::class)

class APResourceResolveServiceImplTest {

    val userBuilder = User.UserBuilder(CharacterLimit(), ApplicationConfig(URL("https://example.com")))
    val postBuilder = Post.PostBuilder(CharacterLimit())

    @Test
    fun `単純な一回のリクエスト`() = runTest {


        val userRepository = mock<UserRepository>()

        val user = UserBuilder.localUserOf()
        whenever(userRepository.findById(any())) doReturn user

        val apRequestService = mock<APRequestService> {
            onBlocking {
                apGet(
                    eq("https"),
                    eq(user),
                    eq(Object::class.java)
                )
            } doReturn dev.usbharu.hideout.domain.model.ap.Object(
                emptyList()
            )
        }
        val apResourceResolveService =
            APResourceResolveServiceImpl(apRequestService, userRepository, InMemoryCacheManager())

        apResourceResolveService.resolve<Object>("https", 0)

        verify(apRequestService, times(1)).apGet(eq("https"), eq(user), eq(Object::class.java))
    }

    @Test
    fun 複数回の同じリクエストが重複して発行されない() = runTest {


        val userRepository = mock<UserRepository>()

        val user = UserBuilder.localUserOf()
        whenever(userRepository.findById(any())) doReturn user

        val apRequestService = mock<APRequestService> {
            onBlocking {
                apGet(
                    eq("https"),
                    eq(user),
                    eq(Object::class.java)
                )
            } doReturn dev.usbharu.hideout.domain.model.ap.Object(
                emptyList()
            )
        }
        val apResourceResolveService =
            APResourceResolveServiceImpl(apRequestService, userRepository, InMemoryCacheManager())

        apResourceResolveService.resolve<Object>("https", 0)
        apResourceResolveService.resolve<Object>("https", 0)
        apResourceResolveService.resolve<Object>("https", 0)
        apResourceResolveService.resolve<Object>("https", 0)

        verify(apRequestService, times(1)).apGet(
            eq("https"),
            eq(user),
            eq(Object::class.java)
        )
    }

    @Test
    fun 複数回の同じリクエストが同時に発行されても重複して発行されない() = runTest {


        val userRepository = mock<UserRepository>()
        val user = UserBuilder.localUserOf()

        whenever(userRepository.findById(any())) doReturn user


        val apRequestService = mock<APRequestService> {
            onBlocking {
                apGet(
                    eq("https"),
                    eq(user),
                    eq(Object::class.java)
                )
            } doReturn dev.usbharu.hideout.domain.model.ap.Object(
                emptyList()
            )
        }
        val apResourceResolveService =
            APResourceResolveServiceImpl(apRequestService, userRepository, InMemoryCacheManager())

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

        verify(apRequestService, times(1)).apGet(
            eq("https"),
            eq(user),
            eq(Object::class.java)
        )
    }

    @Test
    fun 関係のないリクエストは発行する() = runTest {

        val userRepository = mock<UserRepository>()

        val user = UserBuilder.localUserOf()
        whenever(userRepository.findById(any())).doReturn(
            user
        )

        val apRequestService = mock<APRequestService> {
            onBlocking {
                apGet(
                    any(),
                    eq(user),
                    eq(Object::class.java)
                )
            } doReturn dev.usbharu.hideout.domain.model.ap.Object(
                emptyList()
            )
        }

        val apResourceResolveService =
            APResourceResolveServiceImpl(apRequestService, userRepository, InMemoryCacheManager())

        apResourceResolveService.resolve<Object>("abcd", 0)
        apResourceResolveService.resolve<Object>("1234", 0)
        apResourceResolveService.resolve<Object>("aaaa", 0)

        verify(apRequestService, times(3)).apGet(
            any(),
            eq(user),
            eq(Object::class.java)
        )
    }


}
