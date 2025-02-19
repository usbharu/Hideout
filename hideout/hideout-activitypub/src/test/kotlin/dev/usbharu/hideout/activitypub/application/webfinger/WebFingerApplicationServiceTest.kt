package dev.usbharu.hideout.activitypub.application.webfinger

import dev.usbharu.hideout.activitypub.external.activitystreams.TestActorFactory
import dev.usbharu.hideout.activitypub.interfaces.wellknown.Link
import dev.usbharu.hideout.activitypub.interfaces.wellknown.XRD
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import util.TestTransaction
import java.net.URI
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class WebFingerApplicationServiceTest {
    @InjectMocks
    lateinit var webFingerApplicationService: WebFingerApplicationService

    @Mock
    lateinit var actorRepository: ActorRepository

    @Spy
    val transaction = TestTransaction

    @Spy
    val applicationConfig = ApplicationConfig(URI.create("https://example.com"))

    @Test
    fun acctから始まらないとだめ() = runTest {
        assertThrows<IllegalArgumentException> {
            webFingerApplicationService.execute("a", Anonymous)
        }
    }

    @Test
    fun ドメインが自ドメインと一致しないとだめ() = runTest {
        assertThrows<IllegalArgumentException> {
            webFingerApplicationService.execute("acct:test@remote.example.com", Anonymous)
        }
    }

    @Test
    fun `acct@username@hostはだめ`() = runTest {
        assertThrows<IllegalArgumentException> {
            webFingerApplicationService.execute("acct:@username@example.com", Anonymous)
        }
    }

    @Test
    fun actorが存在しないとだめ() = runTest {
        assertThrows<IllegalArgumentException> {
            webFingerApplicationService.execute("acct:test2@example.com", Anonymous)
        }

        verify(actorRepository, times(1)).findByNameAndDomain(eq("test2"), eq("example.com"))
    }

    @Test
    fun actorが存在したら返す() = runTest {
        whenever(
            actorRepository.findByNameAndDomain(
                eq("test"), eq("example.com")
            )
        ).thenReturn(
            TestActorFactory.create(
                actorName = "test",
                domain = "example.com",
                uri = URI.create("https://example.com/users/test")
            )
        )

        val execute = webFingerApplicationService.execute("acct:test@example.com", Anonymous)

        val expected = XRD(
            listOf(
                Link(
                    rel = "self",
                    href = "https://example.com/users/test",
                    type = "application/activity+json",
                    template = null
                )
            ), URI.create("acct:test@example.com")
        )

        assertEquals(expected, execute)
    }
}