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

package dev.usbharu.hideout.activitypub.service.activity.block

import dev.usbharu.hideout.activitypub.domain.model.Block
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Reject
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.DeliverBlockJob
import dev.usbharu.hideout.core.external.job.DeliverBlockJobParam
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
class APDeliverBlockJobProcessorTest {

    @Mock
    private lateinit var apRequestService: APRequestService

    @Mock
    private lateinit var actorRepository: ActorRepository

    @Spy
    private val transaction = TestTransaction

    @Mock
    private lateinit var deliverBlockJob: DeliverBlockJob

    @InjectMocks
    private lateinit var apDeliverBlockJobProcessor: APDeliverBlockJobProcessor

    @Test
    fun `process rejectとblockがapPostされる`() = runTest {
        val user = UserBuilder.localUserOf()
        whenever(actorRepository.findById(eq(user.id))).doReturn(user)


        val block = Block(
            actor = user.url,
            "https://example.com/block",
            apObject = "https://remote.example.com"
        )
        val reject = Reject(
            actor = user.url,
            "https://example.com/reject/follow",
            apObject = Follow(
                apObject = user.url,
                actor = "https://remote.example.com"
            )
        )
        val param = DeliverBlockJobParam(
            user.id,
            block,
            reject,
            "https://remote.example.com"
        )


        apDeliverBlockJobProcessor.process(param)

        verify(apRequestService, times(1)).apPost(eq("https://remote.example.com"), eq(block), eq(user))
        verify(apRequestService, times(1)).apPost(eq("https://remote.example.com"), eq(reject), eq(user))
    }

    @Test
    fun `job deliverBlockJobが返ってくる`() {
        val actual = apDeliverBlockJobProcessor.job()
        assertThat(actual).isEqualTo(deliverBlockJob)
    }
}
