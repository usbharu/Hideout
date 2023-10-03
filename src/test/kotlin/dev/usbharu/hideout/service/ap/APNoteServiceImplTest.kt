@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.config.ConfigData
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.job.JobQueueParentService
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
import utils.JsonObjectMapper.objectMapper
import utils.TestApplicationConfig.testApplicationConfig
import java.time.Instant
import kotlin.test.assertEquals

class APNoteServiceImplTest {
    @Test
    fun `createPost 新しい投稿`() = runTest {
        val followers = listOf(
            User.of(
                2L,
                "follower",
                "follower.example.com",
                "followerUser",
                "test follower user",
                "https://follower.example.com/inbox",
                "https://follower.example.com/outbox",
                "https://follower.example.com",
                "https://follower.example.com",
                publicKey = "",
                createdAt = Instant.now()
            ),
            User.of(
                3L,
                "follower2",
                "follower2.example.com",
                "follower2User",
                "test follower2 user",
                "https://follower2.example.com/inbox",
                "https://follower2.example.com/outbox",
                "https://follower2.example.com",
                "https://follower2.example.com",
                publicKey = "",
                createdAt = Instant.now()
            )
        )
        val userQueryService = mock<UserQueryService> {
            onBlocking { findById(eq(1L)) } doReturn User.of(
                1L,
                "test",
                "example.com",
                "testUser",
                "test user",
                "a",
                "https://example.com/inbox",
                "https://example.com/outbox",
                "https://example.com",
                publicKey = "",
                privateKey = "a",
                createdAt = Instant.now()
            )
        }
        val followerQueryService = mock<FollowerQueryService> {
            onBlocking { findFollowersById(eq(1L)) } doReturn followers
        }
        val jobQueueParentService = mock<JobQueueParentService>()
        val activityPubNoteService =
            APNoteServiceImpl(
                httpClient = mock(),
                jobQueueParentService = jobQueueParentService,
                postRepository = mock(),
                apUserService = mock(),
                userQueryService = userQueryService,
                followerQueryService = followerQueryService,
                postQueryService = mock(),
                objectMapper = objectMapper,
                applicationConfig = testApplicationConfig,
                postService = mock(),
            )
        val postEntity = Post.of(
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
        val activityPubNoteService = APNoteServiceImpl(
            httpClient = httpClient,
            jobQueueParentService = mock(),
            postRepository = mock(),
            apUserService = mock(),
            userQueryService = mock(),
            followerQueryService = mock(),
            postQueryService = mock(),
            objectMapper = objectMapper,
            applicationConfig = testApplicationConfig,
            postService = mock(),
        )
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
