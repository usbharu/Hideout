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
