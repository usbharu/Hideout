package dev.usbharu.hideout.activitypub.service.activity.follow

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.core.service.follow.SendFollowDto
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import utils.UserBuilder

class APSendFollowServiceImplTest {
    @Test
    fun `sendFollow フォローするユーザーのinboxにFollowオブジェクトが送られる`() = runTest {
        val apRequestService = mock<APRequestService>()
        val apSendFollowServiceImpl = APSendFollowServiceImpl(apRequestService)

        val sendFollowDto = SendFollowDto(
            UserBuilder.localUserOf(),
            UserBuilder.remoteUserOf()
        )
        apSendFollowServiceImpl.sendFollow(sendFollowDto)

        val value = Follow(
            `object` = sendFollowDto.followTargetUserId.url,
            actor = sendFollowDto.userId.url
        )
        verify(apRequestService, times(1)).apPost(
            eq(sendFollowDto.followTargetUserId.inbox),
            eq(value),
            eq(sendFollowDto.userId)
        )
    }
}
