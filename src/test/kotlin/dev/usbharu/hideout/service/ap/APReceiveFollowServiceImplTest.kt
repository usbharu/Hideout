@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.config.ConfigData
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.ap.Image
import dev.usbharu.hideout.domain.model.ap.Key
import dev.usbharu.hideout.domain.model.ap.Person
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.job.ReceiveFollowJob
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.job.JobQueueParentService
import dev.usbharu.hideout.service.user.UserService
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
import java.time.Instant

class APReceiveFollowServiceImplTest {
    @Test
    fun `receiveFollow フォロー受付処理`() = runTest {
        val jobQueueParentService = mock<JobQueueParentService> {
            onBlocking { schedule(eq(ReceiveFollowJob), any()) } doReturn Unit
        }
        val activityPubFollowService =
            APReceiveFollowServiceImpl(
                jobQueueParentService,
                mock(),
                mock(),
                mock(),
                TestTransaction,
                objectMapper,
                mock()
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
        Config.configData = ConfigData()
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
                    User.of(
                        id = 1L,
                        name = "test",
                        domain = "example.com",
                        screenName = "testUser",
                        description = "This user is test user.",
                        inbox = "https://example.com/inbox",
                        outbox = "https://example.com/outbox",
                        url = "https://example.com",
                        publicKey = "",
                        createdAt = Instant.now(),
                        keyId = "a"
                    )
            onBlocking { findByUrl(eq("https://follower.example.com")) } doReturn
                    User.of(
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
            APReceiveFollowServiceImpl(
                mock(),
                apUserService,
                userService,
                userQueryService,
                TestTransaction,
                objectMapper,
                mock()
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
