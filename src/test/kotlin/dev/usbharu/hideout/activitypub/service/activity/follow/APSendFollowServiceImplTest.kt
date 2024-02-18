/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
