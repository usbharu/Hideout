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

package dev.usbharu.hideout.activitypub.service.activity.create

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteServiceImpl
import dev.usbharu.hideout.application.config.ActivityPubConfig
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.DeliverPostJob
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.PostBuilder
import utils.UserBuilder
import java.net.URL
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class ApSendCreateServiceImplTest {

    @Mock
    private lateinit var followerQueryService: FollowerQueryService

    @Spy
    private val objectMapper: ObjectMapper = ActivityPubConfig().objectMapper()

    @Mock
    private lateinit var jobQueueParentService: JobQueueParentService

    @Mock
    private lateinit var actorRepository: ActorRepository

    @Mock
    private lateinit var noteQueryService: NoteQueryService

    @Spy
    private val applicationConfig: ApplicationConfig = ApplicationConfig(URL("https://example.com"))

    @InjectMocks
    private lateinit var apSendCreateServiceImpl: ApSendCreateServiceImpl

    @Test
    fun `createNote 正常なPostでCreateのジョブを発行できる`() = runTest {
        val post = PostBuilder.of()
        val user = UserBuilder.localUserOf(id = post.actorId)
        val note = Note(
            id = post.apId,
            attributedTo = user.url,
            content = post.text,
            published = Instant.ofEpochMilli(post.createdAt).toString(),
            to = listOfNotNull(APNoteServiceImpl.public, user.followers),
            sensitive = post.sensitive,
            cc = listOfNotNull(APNoteServiceImpl.public, user.followers),
            inReplyTo = null
        )
        val followers = listOf(
            UserBuilder.remoteUserOf(),
            UserBuilder.remoteUserOf(),
            UserBuilder.remoteUserOf()
        )

        whenever(followerQueryService.findFollowersById(eq(post.actorId))).doReturn(followers)
        whenever(actorRepository.findById(eq(post.actorId))).doReturn(user)
        whenever(noteQueryService.findById(eq(post.id))).doReturn(note to post)

        apSendCreateServiceImpl.createNote(post)

        verify(jobQueueParentService, times(followers.size)).schedule(eq(DeliverPostJob), any())
    }
}
