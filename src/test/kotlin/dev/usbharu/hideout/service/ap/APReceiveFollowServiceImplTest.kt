@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Image
import dev.usbharu.hideout.activitypub.domain.model.Key
import dev.usbharu.hideout.activitypub.domain.model.Person
import dev.usbharu.hideout.activitypub.service.activity.follow.APReceiveFollowJobServiceImpl
import dev.usbharu.hideout.activitypub.service.activity.follow.APReceiveFollowServiceImpl
import dev.usbharu.hideout.activitypub.service.`object`.user.APUserService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.config.CharacterLimit
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.external.job.ReceiveFollowJob
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import dev.usbharu.hideout.core.service.user.UserService
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import utils.JsonObjectMapper.objectMapper
import utils.TestTransaction
import java.net.URL
import java.time.Instant

class APReceiveFollowServiceImplTest {

    val userBuilder = User.UserBuilder(CharacterLimit(), ApplicationConfig(URL("https://example.com")))
    val postBuilder = Post.PostBuilder(CharacterLimit())

    @Test
    fun `receiveFollow フォロー受付処理`() = runTest {
        val jobQueueParentService = mock<JobQueueParentService> {
            onBlocking { schedule(eq(ReceiveFollowJob), any()) } doReturn Unit
        }
        val activityPubFollowService =
            APReceiveFollowServiceImpl(
                jobQueueParentService,
                objectMapper
            )
        activityPubFollowService.receiveFollow(
            Follow(
                emptyList(),
                "Follow",
                "https://example.com",
                "https://follower.example.com"
            )
        )
        verify(jobQueueParentService, times(1)).schedule(eq(ReceiveFollowJob), any())
        argumentCaptor<ScheduleContext<ReceiveFollowJob>.(ReceiveFollowJob) -> Unit> {
            verify(jobQueueParentService, times(1)).schedule(eq(ReceiveFollowJob), capture())
            val scheduleContext = ScheduleContext<ReceiveFollowJob>(Json)
            firstValue.invoke(scheduleContext, ReceiveFollowJob)
            val actor = scheduleContext.props.props[ReceiveFollowJob.actor.name]
            val targetActor = scheduleContext.props.props[ReceiveFollowJob.targetActor.name]
            val follow = scheduleContext.props.props[ReceiveFollowJob.follow.name] as String
            assertEquals("https://follower.example.com", actor)
            assertEquals("https://example.com", targetActor)
            //language=JSON
            assertEquals(
                Json.parseToJsonElement(
                    """{
  "type": "Follow",
  "name": "Follow",
  "actor": "https://follower.example.com",
  "object": "https://example.com",
  "@context": null
}"""
                ),
                Json.parseToJsonElement(follow)
            )
        }
    }

    @Test
    fun `receiveFollowJob フォロー受付処理のJob`() = runTest {
        val person = Person(
            type = emptyList(),
            name = "follower",
            id = "https://follower.example.com",
            preferredUsername = "followerUser",
            summary = "This user is follower user.",
            inbox = "https://follower.example.com/inbox",
            outbox = "https://follower.example.com/outbox",
            url = "https://follower.example.com",
            icon = Image(
                type = emptyList(),
                name = "https://follower.example.com/image",
                mediaType = "image/png",
                url = "https://follower.example.com/image"
            ),
            publicKey = Key(
                type = emptyList(),
                name = "Public Key",
                id = "https://follower.example.com#main-key",
                owner = "https://follower.example.com",
                publicKeyPem = "BEGIN PUBLIC KEY...END PUBLIC KEY",
            ),
            followers = "",
            following = ""

        )
        val apUserService = mock<APUserService> {
            onBlocking { fetchPerson(anyString(), any()) } doReturn person
        }
        val userQueryService = mock<UserQueryService> {
            onBlocking { findByUrl(eq("https://example.com")) } doReturn
                    userBuilder.of(
                        id = 1L,
                        name = "test",
                        domain = "example.com",
                        screenName = "testUser",
                        description = "This user is test user.",
                        inbox = "https://example.com/inbox",
                        outbox = "https://example.com/outbox",
                        url = "https://example.com",
                        publicKey = "",
                        password = "a",
                        privateKey = "a",
                        createdAt = Instant.now(),
                        keyId = "a"
                    )
            onBlocking { findByUrl(eq("https://follower.example.com")) } doReturn
                    userBuilder.of(
                        id = 2L,
                        name = "follower",
                        domain = "follower.example.com",
                        screenName = "followerUser",
                        description = "This user is test follower user.",
                        inbox = "https://follower.example.com/inbox",
                        outbox = "https://follower.example.com/outbox",
                        url = "https://follower.example.com",
                        publicKey = "",
                        createdAt = Instant.now(),
                        keyId = "a"
                    )
        }

        val userService = mock<UserService> {
            onBlocking { followRequest(any(), any()) } doReturn false
        }
        val activityPubFollowService =
            APReceiveFollowJobServiceImpl(
                apUserService,
                userQueryService,
                mock(),
                userService,
                objectMapper,
                TestTransaction
            )
        activityPubFollowService.receiveFollowJob(
            JobProps(
                data = mapOf<String, Any>(
                    ReceiveFollowJob.actor.name to "https://follower.example.com",
                    ReceiveFollowJob.targetActor.name to "https://example.com",
                    //language=JSON
                    ReceiveFollowJob.follow.name to """{
  "type": "Follow",
  "name": "Follow",
  "object": "https://example.com",
  "actor": "https://follower.example.com",
  "@context": null
}"""
                ),
                json = Json
            )
        )
    }
}
