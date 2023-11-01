package dev.usbharu.hideout.service.ap.resource

import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.activitypub.service.common.APResourceResolveServiceImpl
import dev.usbharu.hideout.activitypub.service.common.InMemoryCacheManager
import dev.usbharu.hideout.activitypub.service.common.resolve
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.config.CharacterLimit
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.domain.model.user.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.UserBuilder
import java.net.URL
import dev.usbharu.hideout.activitypub.domain.model.`object`.Object as APObject

@ExtendWith(MockitoExtension::class)

class APResourceResolveServiceImplTest {

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
                    eq(APObject::class.java)
                )
            } doReturn APObject(
                emptyList()
            )
        }
        val apResourceResolveService =
            APResourceResolveServiceImpl(apRequestService, userRepository, InMemoryCacheManager())

        apResourceResolveService.resolve<APObject>("https", 0)

        verify(apRequestService, times(1)).apGet(eq("https"), eq(user), eq(APObject::class.java))
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
                    eq(APObject::class.java)
                )
            } doReturn APObject(
                emptyList()
            )
        }
        val apResourceResolveService =
            APResourceResolveServiceImpl(apRequestService, userRepository, InMemoryCacheManager())

        apResourceResolveService.resolve<APObject>("https", 0)
        apResourceResolveService.resolve<APObject>("https", 0)
        apResourceResolveService.resolve<APObject>("https", 0)
        apResourceResolveService.resolve<APObject>("https", 0)

        verify(apRequestService, times(1)).apGet(
            eq("https"),
            eq(user),
            eq(APObject::class.java)
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
                    eq(APObject::class.java)
                )
            } doReturn APObject(
                emptyList()
            )
        }
        val apResourceResolveService =
            APResourceResolveServiceImpl(apRequestService, userRepository, InMemoryCacheManager())

        repeat(10) {
            awaitAll(
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
            )
        }

        verify(apRequestService, times(1)).apGet(
            eq("https"),
            eq(user),
            eq(APObject::class.java)
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
                    eq(APObject::class.java)
                )
            } doReturn APObject(
                emptyList()
            )
        }

        val apResourceResolveService =
            APResourceResolveServiceImpl(apRequestService, userRepository, InMemoryCacheManager())

        apResourceResolveService.resolve<APObject>("abcd", 0)
        apResourceResolveService.resolve<APObject>("1234", 0)
        apResourceResolveService.resolve<APObject>("aaaa", 0)

        verify(apRequestService, times(3)).apGet(
            any(),
            eq(user),
            eq(APObject::class.java)
        )
    }


}
