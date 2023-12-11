package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.core.external.job.DeliverAcceptJob
import dev.usbharu.hideout.core.external.job.DeliverAcceptJobParam
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import utils.UserBuilder

@ExtendWith(MockitoExtension::class)
class ApSendAcceptServiceImplTest {

    @Mock
    private lateinit var jobQueueParentService: JobQueueParentService

    @Mock
    private lateinit var deliverAcceptJob: DeliverAcceptJob

    @InjectMocks
    private lateinit var apSendAcceptServiceImpl: ApSendAcceptServiceImpl

    @Test
    fun `sendAccept DeliverAcceptJobが発行される`() = runTest {
        val user = UserBuilder.localUserOf()
        val remoteUser = UserBuilder.remoteUserOf()

        apSendAcceptServiceImpl.sendAcceptFollow(user, remoteUser)

        val deliverAcceptJobParam = DeliverAcceptJobParam(
            Accept(apObject = Follow(apObject = user.url, actor = remoteUser.url), actor = user.url),
            remoteUser.inbox,
            user.id
        )
        verify(jobQueueParentService, times(1)).scheduleTypeSafe(eq(deliverAcceptJob), eq(deliverAcceptJobParam))
    }
}
