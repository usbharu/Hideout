@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.config.CharacterLimit
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.MediaQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.ap.job.ApNoteJobServiceImpl
import dev.usbharu.hideout.service.job.JobQueueParentService
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import kjob.core.job.JobProps
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.eq
import org.mockito.kotlin.*
import utils.JsonObjectMapper.objectMapper
import utils.TestTransaction
import java.net.URL
import java.time.Instant
import kotlin.test.assertEquals

class APNoteServiceImplTest {

    val userBuilder = User.UserBuilder(CharacterLimit(), ApplicationConfig(URL("https://example.com")))
    val postBuilder = Post.PostBuilder(CharacterLimit())

    @Test
    fun `createPost 新しい投稿`() {
        val mediaQueryService = mock<MediaQueryService> {
            onBlocking { findByPostId(anyLong()) } doReturn emptyList()
        }



        runTest {
            val followers = listOf(
                userBuilder.of(
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
                    createdAt = Instant.now(),
                    keyId = "a"
                ),
                userBuilder.of(
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
                    createdAt = Instant.now(),
                    keyId = "a"
                )
            )
            val userQueryService = mock<UserQueryService> {
                onBlocking { findById(eq(1L)) } doReturn userBuilder.of(
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
                    createdAt = Instant.now(),
                    keyId = "a"
                )
            }
            val followerQueryService = mock<FollowerQueryService> {
                onBlocking { findFollowersById(eq(1L)) } doReturn followers
            }
            val jobQueueParentService = mock<JobQueueParentService>()
            val activityPubNoteService =
                APNoteServiceImpl(
                    jobQueueParentService = jobQueueParentService,
                    postRepository = mock(),
                    apUserService = mock(),
                    userQueryService = userQueryService,
                    followerQueryService = followerQueryService,
                    postQueryService = mock(),
                    mediaQueryService = mediaQueryService,
                    objectMapper = objectMapper,
                    postService = mock(),
                    apResourceResolveService = mock(),
                    postBuilder = postBuilder
                )
            val postEntity = postBuilder.of(
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
    }

    @Test
    fun `createPostJob 新しい投稿のJob`() {
        runTest {
            val mediaQueryService = mock<MediaQueryService> {
                onBlocking { findByPostId(anyLong()) } doReturn emptyList()
            }

            val httpClient = HttpClient(
                MockEngine { httpRequestData ->
                    assertEquals("https://follower.example.com/inbox", httpRequestData.url.toString())
                    respondOk()
                }
            )
            val activityPubNoteService = ApNoteJobServiceImpl(

                userQueryService = mock(),
                objectMapper = objectMapper,
                apRequestService = mock(),
                transaction = TestTransaction,
                applicationConfig = ApplicationConfig(URL("https://example.com"))
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
                        DeliverPostJob.inbox.name to "https://follower.example.com/inbox",
                        DeliverPostJob.media.name to "[]"
                    ),
                    json = Json
                )
            )
        }
    }
}
