@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.config.ConfigData
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import dev.usbharu.hideout.service.job.JobQueueParentService
import dev.usbharu.hideout.service.user.IUserService
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import kjob.core.job.JobProps
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.mockito.Mockito.eq
import org.mockito.kotlin.*
import utils.JsonObjectMapper
import java.time.Instant
import kotlin.test.assertEquals

class ActivityPubNoteServiceImplTest {
    @Test
    fun `createPost 新しい投稿`() = runTest {
        val followers = listOf<User>(
            User(
                2L,
                "follower",
                "follower.example.com",
                "followerUser",
                "test follower user",
                "https://follower.example.com/inbox",
                "https://follower.example.com/outbox",
                "https://follower.example.com",
                "",
                publicKey = "",
                createdAt = Instant.now()
            ),
            User(
                3L,
                "follower2",
                "follower2.example.com",
                "follower2User",
                "test follower2 user",
                "https://follower2.example.com/inbox",
                "https://follower2.example.com/outbox",
                "https://follower2.example.com",
                "",
                publicKey = "",
                createdAt = Instant.now()
            )
        )
        val userService = mock<IUserService> {
            onBlocking { findById(eq(1L)) } doReturn User(
                1L,
                "test",
                "example.com",
                "testUser",
                "test user",
                "https://example.com/inbox",
                "https://example.com/outbox",
                "https:.//example.com",
                "",
                publicKey = "",
                createdAt = Instant.now()
            )
            onBlocking { findFollowersById(eq(1L)) } doReturn followers
        }
        val jobQueueParentService = mock<JobQueueParentService>()
        val activityPubNoteService =
            ActivityPubNoteServiceImpl(mock(), jobQueueParentService, userService, mock(), mock())
        val postEntity = Post(
            1L,
            1L,
            null,
            "test text",
            1L,
            Visibility.PUBLIC,
            "https://example.com"
        )
        activityPubNoteService.createNote(postEntity)
        verify(jobQueueParentService, times(2)).schedule(eq(DeliverPostJob), any())
    }

    @Test
    fun `createPostJob 新しい投稿のJob`() = runTest {
        Config.configData = ConfigData(objectMapper = JsonObjectMapper.objectMapper)
        val httpClient = HttpClient(
            MockEngine { httpRequestData ->
                assertEquals("https://follower.example.com/inbox", httpRequestData.url.toString())
                respondOk()
            }
        )
        val activityPubNoteService = ActivityPubNoteServiceImpl(httpClient, mock(), mock(), mock(), mock())
        activityPubNoteService.createNoteJob(
            JobProps(
                data = mapOf<String, Any>(
                    DeliverPostJob.actor.name to "https://follower.example.com",
                    DeliverPostJob.post.name to """{
  "id": 1,
  "userId": 1,
  "text": "test text",
  "createdAt": 132525324,
  "visibility": 0,
  "url": "https://example.com"
}""",
                    DeliverPostJob.inbox.name to "https://follower.example.com/inbox"
                ),
                json = Json
            )
        )
    }
}
