@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.usbharu.hideout.service.ap.job

import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.activitypub.service.`object`.note.APNoteServiceImpl
import dev.usbharu.hideout.activitypub.service.`object`.note.ApNoteJobServiceImpl
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.external.job.DeliverPostJob
import dev.usbharu.hideout.core.query.UserQueryService
import kjob.core.job.JobProps
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import utils.JsonObjectMapper
import utils.TestTransaction
import utils.UserBuilder
import java.net.URL
import java.time.Instant

class ApNoteJobServiceImplTest {
    @Test
    fun `createPostJob 新しい投稿のJob`() = runTest {
        val apRequestService = mock<APRequestService>()
        val user = UserBuilder.localUserOf()
        val userQueryService = mock<UserQueryService> {
            onBlocking { findByUrl(eq(user.url)) } doReturn user
        }
        val activityPubNoteService = ApNoteJobServiceImpl(

            userQueryService = userQueryService,
            objectMapper = JsonObjectMapper.objectMapper,
            apRequestService = apRequestService,
            transaction = TestTransaction,
            applicationConfig = ApplicationConfig(URL("https://example.com"))
        )
        val remoteUserOf = UserBuilder.remoteUserOf()
        activityPubNoteService.createNoteJob(
            JobProps(
                data = mapOf<String, Any>(
                    DeliverPostJob.actor.name to user.url,
                    DeliverPostJob.post.name to """{
                          "id": 1,
                          "userId": ${user.id},
                          "text": "test text",
                          "createdAt": 132525324,
                          "visibility": 0,
                          "url": "https://example.com"
                        }""",
                    DeliverPostJob.inbox.name to remoteUserOf.inbox,
                    DeliverPostJob.media.name to "[]"
                ), json = Json
            )
        )

        val note = Note(
            name = "Note",
            id = "https://example.com",
            attributedTo = user.url,
            content = "test text",
            published = Instant.ofEpochMilli(132525324).toString(),
            to = listOfNotNull(APNoteServiceImpl.public, user.followers)
        )
        val create = Create(
            name = "Create Note",
            `object` = note,
            actor = note.attributedTo,
            id = "https://example.com/create/note/1"
        )
        verify(apRequestService, times(1)).apPost(
            eq(remoteUserOf.inbox),
            eq(create),
            eq(user)
        )
    }
}
