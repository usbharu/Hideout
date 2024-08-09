package dev.usbharu.hideout.core.application.post

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.FromApi
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.infrastructure.factory.PostFactoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import utils.TestTransaction

@ExtendWith(MockitoExtension::class)
class RegisterLocalPostApplicationServiceTest {
    @InjectMocks
    lateinit var service: RegisterLocalPostApplicationService

    @Mock
    lateinit var actorRepository: ActorRepository

    @Mock
    lateinit var postRepository: PostRepository

    @Mock
    lateinit var postFactoryImpl: PostFactoryImpl

    @Spy
    val transaction = TestTransaction

    @Test
    fun postを作成できる() = runTest {
        val actor = TestActorFactory.create(id = 1)
        whenever(actorRepository.findById(ActorId(1))) doReturn actor
        whenever(
            postFactoryImpl.createLocal(
                eq(actor),
            )
        )

        service.execute(
            RegisterLocalPost("content test", null, Visibility.PUBLIC, null, false, emptyList<>()), FromApi(
                ActorId(1), UserDetailId(1), Acct("test", "example.com")
            )
        )
    }
}