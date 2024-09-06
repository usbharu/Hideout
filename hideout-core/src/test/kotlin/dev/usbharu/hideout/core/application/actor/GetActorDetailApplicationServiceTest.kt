package dev.usbharu.hideout.core.application.actor

import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import dev.usbharu.hideout.core.domain.model.media.*
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.TestTransaction
import java.net.URI

@ExtendWith(MockitoExtension::class)
class GetActorDetailApplicationServiceTest {

    @InjectMocks
    lateinit var service: GetActorDetailApplicationService

    @Mock
    lateinit var actorRepository: ActorRepository

    @Mock
    lateinit var mediaRepository: MediaRepository

    @Spy
    val applicationConfig = ApplicationConfig(URI.create("http://localhost:8081").toURL())

    @Spy
    val transaction = TestTransaction

    @Test
    fun idとname両方nullならエラー() = runTest {
        assertThrows<IllegalArgumentException> {
            service.execute(GetActorDetail(null, null), Anonymous)
        }
    }

    @Test
    fun idがnullじゃない場合idから取得() = runTest {
        val actor = TestActorFactory.create(id = 1)
        whenever(actorRepository.findById(ActorId(1))).doReturn(actor)

        val actual = service.execute(GetActorDetail(null, 1), Anonymous)

        assertEquals(actual, ActorDetail.of(actor, null, null))
    }

    @Test
    fun idが存在しないとエラー() = runTest {
        assertThrows<IllegalArgumentException> {
            service.execute(GetActorDetail(null, 2), Anonymous)
        }
    }

    @Test
    fun idがnullでacctがnullじゃない場合acctから取得() = runTest {
        val actor = TestActorFactory.create(actorName = "test", domain = "example.com")
        whenever(actorRepository.findByNameAndDomain("test", "example.com")).thenReturn(actor)

        val actual = service.execute(GetActorDetail(Acct("test", "example.com"), null), Anonymous)

        assertEquals(actual, ActorDetail.of(actor, null, null))
    }

    @Test
    fun acctのhostが空ならローカルのhostが使われる() = runTest {
        val actor = TestActorFactory.create(actorName = "test", domain = "localhost")
        whenever(actorRepository.findByNameAndDomain("test", "localhost")).thenReturn(actor)

        val actual = service.execute(GetActorDetail(Acct("test", ""), null), Anonymous)

        assertEquals(actual, ActorDetail.of(actor, null, null))
    }

    @Test
    fun acctが存在しないとエラー() = runTest {
        assertThrows<IllegalArgumentException> {
            service.execute(GetActorDetail(Acct("test", "example.com"), null), Anonymous)
        }
    }

    @Test
    fun idとacctがnullじゃない場合idが優先される() = runTest {
        val actor = TestActorFactory.create(id = 1)
        whenever(actorRepository.findById(ActorId(1))).doReturn(actor)

        val actual = service.execute(GetActorDetail(null, 1), Anonymous)

        assertEquals(actual, ActorDetail.of(actor, null, null))
        verify(actorRepository, never()).findByNameAndDomain(any(), any())
    }

    @Test
    fun iconがnullじゃない時取得する() = runTest {
        val actor = TestActorFactory.create(id = 1, icon = 1)
        whenever(actorRepository.findById(ActorId(1))).doReturn(actor)
        whenever(mediaRepository.findById(MediaId(1))).doReturn(
            Media(
                id = MediaId(1),
                name = MediaName(""),
                url = URI.create("http://example.com"),
                remoteUrl = null,
                thumbnailUrl = null,
                type = FileType.Image,
                mimeType = MimeType("image", "jpeg", FileType.Image),
                blurHash = null,
                description = null,
                actorId = ActorId(1)
            )
        )

        val actual = service.execute(GetActorDetail(null, 1), Anonymous)

        assertEquals(actual, ActorDetail.of(actor, URI.create("http://example.com"), null))
    }

    @Test
    fun bannerがnullじゃない時取得する() = runTest {
        val actor = TestActorFactory.create(id = 1, banner = 1)
        whenever(actorRepository.findById(ActorId(1))).doReturn(actor)
        whenever(mediaRepository.findById(MediaId(1))).doReturn(
            Media(
                id = MediaId(1),
                name = MediaName(""),
                url = URI.create("http://example.com"),
                remoteUrl = null,
                thumbnailUrl = null,
                type = FileType.Image,
                mimeType = MimeType("image", "jpeg", FileType.Image),
                blurHash = null,
                description = null,
                actorId = ActorId(1)
            )
        )

        val actual = service.execute(GetActorDetail(null, 1), Anonymous)

        assertEquals(actual, ActorDetail.of(actor, null, URI.create("http://example.com")))
    }

    @Test
    fun iconとbannerが見つからなかった場合null() = runTest {
        val actor = TestActorFactory.create(id = 1, icon = 1, banner = 1)
        whenever(actorRepository.findById(ActorId(1))).doReturn(actor)

        val actual = service.execute(GetActorDetail(null, 1), Anonymous)

        assertEquals(actual, ActorDetail.of(actor, null, null))
    }
}