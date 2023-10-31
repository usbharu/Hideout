package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.hideout.dto.SendFollowDto
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
            name = "Follow",
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
