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
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.DeliverAcceptJob
import dev.usbharu.hideout.core.external.job.DeliverAcceptJobParam
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
    private lateinit var actorRepository: ActorRepository

    @Mock
    private lateinit var deliverAcceptJob: DeliverAcceptJob

    @Spy
    private val transaction = TestTransaction

    @InjectMocks
    private lateinit var apDeliverAcceptJobProcessor: APDeliverAcceptJobProcessor

    @Test
    fun `process apPostが発行される`() = runTest {
        val user = UserBuilder.localUserOf()

        whenever(actorRepository.findById(eq(1))).doReturn(user)

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
