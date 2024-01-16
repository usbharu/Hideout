package dev.usbharu.hideout.activitypub.service.activity.follow

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.service.follow.SendFollowDto
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import utils.UserBuilder
import java.net.URL

class APSendFollowServiceImplTest {
    @Test
    fun `sendFollow フォローするユーザーのinboxにFollowオブジェクトが送られる`() = runTest {
        val apRequestService = mock<APRequestService>()
        val applicationConfig = ApplicationConfig(URL("https://example.com"))
        val apSendFollowServiceImpl = APSendFollowServiceImpl(apRequestService, applicationConfig)

        val sendFollowDto = SendFollowDto(
            UserBuilder.localUserOf(),
            UserBuilder.remoteUserOf()
        )
        apSendFollowServiceImpl.sendFollow(sendFollowDto)

        val value = Follow(
            apObject = sendFollowDto.followTargetActorId.url,
            actor = sendFollowDto.actorId.url,
            id = "${applicationConfig.url}/follow/${sendFollowDto.actorId.id}/${sendFollowDto.followTargetActorId.id}"
        )
        verify(apRequestService, times(1)).apPost(
            eq(sendFollowDto.followTargetActorId.inbox),
            eq(value),
            eq(sendFollowDto.actorId)
        )
    }
}
