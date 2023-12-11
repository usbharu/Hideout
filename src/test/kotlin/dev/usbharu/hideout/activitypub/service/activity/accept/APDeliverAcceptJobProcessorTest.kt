package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.core.external.job.DeliverAcceptJob
import dev.usbharu.hideout.core.external.job.DeliverAcceptJobParam
import dev.usbharu.hideout.core.query.UserQueryService
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.TestTransaction
import utils.UserBuilder

@ExtendWith(MockitoExtension::class)
class APDeliverAcceptJobProcessorTest {

    @Mock
    private lateinit var apRequestService: APRequestService

    @Mock
    private lateinit var userQueryService: UserQueryService

    @Mock
    private lateinit var deliverAcceptJob: DeliverAcceptJob

    @Spy
    private val transaction = TestTransaction

    @InjectMocks
    private lateinit var apDeliverAcceptJobProcessor: APDeliverAcceptJobProcessor

    @Test
    fun `process apPostが発行される`() = runTest {
        val user = UserBuilder.localUserOf()

        whenever(userQueryService.findById(eq(1))).doReturn(user)

        val accept = Accept(
            apObject = Follow(
                apObject = "https://example.com",
                actor = "https://remote.example.com"
            ),
            actor = "https://example.com"
        )
        val param = DeliverAcceptJobParam(
            accept = accept,
            "https://remote.example.com",
            1
        )

        apDeliverAcceptJobProcessor.process(param)

        verify(apRequestService, times(1)).apPost(eq("https://remote.example.com"), eq(accept), eq(user))
    }

    @Test
    fun `job DeliverAcceptJobが返ってくる`() {
        val actual = apDeliverAcceptJobProcessor.job()

        assertThat(actual).isEqualTo(deliverAcceptJob)

    }
}
