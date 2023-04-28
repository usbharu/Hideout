@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.usbharu.hideout.service.activitypub

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.config.ConfigData
import dev.usbharu.hideout.domain.model.ap.*
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.job.ReceiveFollowJob
import dev.usbharu.hideout.service.impl.IUserService
import dev.usbharu.hideout.service.job.JobQueueParentService
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import utils.JsonObjectMapper
import java.time.Instant

class ActivityPubFollowServiceImplTest {
    @Test
    fun `receiveFollow フォロー受付処理`() = runTest {
        val jobQueueParentService = mock<JobQueueParentService> {
            onBlocking { schedule(eq(ReceiveFollowJob), any()) } doReturn Unit
        }
        val activityPubFollowService = ActivityPubFollowServiceImpl(jobQueueParentService, mock(), mock(), mock())
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
            val follow = scheduleContext.props.props[ReceiveFollowJob.follow.name]
            assertEquals("https://follower.example.com", actor)
            assertEquals("https://example.com", targetActor)
            assertEquals(
                """{"type":"Follow","name":"Follow","object":"https://example.com","actor":"https://follower.example.com","@context":null}""",
                follow
            )
        }
    }

    @Test
    fun `receiveFollowJob フォロー受付処理のJob`() = runTest {
        Config.configData = ConfigData(objectMapper = JsonObjectMapper.objectMapper)
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
            )

        )
        val activityPubUserService = mock<ActivityPubUserService> {
            onBlocking { fetchPerson(anyString()) } doReturn person
        }
        val userService = mock<IUserService> {
            onBlocking { findByUrls(any()) } doReturn listOf(
                User(
                    id = 1L,
                    name = "test",
                    domain = "example.com",
                    screenName = "testUser",
                    description = "This user is test user.",
                    inbox = "https://example.com/inbox",
                    outbox = "https://example.com/outbox",
                    url = "https://example.com",
                    publicKey = "",
                    createdAt = Instant.now()
                ),
                User(
                    id = 2L,
                    name = "follower",
                    domain = "follower.example.com",
                    screenName = "followerUser",
                    description = "This user is test follower user.",
                    inbox = "https://follower.example.com/inbox",
                    outbox = "https://follower.example.com/outbox",
                    url = "https://follower.example.com",
                    publicKey = "",
                    createdAt = Instant.now()
                )
            )
            onBlocking { addFollowers(any(), any()) } doReturn Unit
        }
        val activityPubFollowService =
            ActivityPubFollowServiceImpl(
                mock(),
                activityPubUserService,
                userService,
                HttpClient(MockEngine { httpRequestData ->
                    assertEquals(person.inbox, httpRequestData.url.toString())
                    val accept = Accept(
                        type = emptyList(),
                        name = "Follow",
                        `object` = Follow(
                            type = emptyList(),
                            name = "Follow",
                            `object` = "https://example.com",
                            actor = "https://follower.example.com"
                        ),
                        actor = "https://example.com"
                    )
                    accept.context += "https://www.w3.org/ns/activitystreams"
                    assertEquals(
                        accept,
                        Config.configData.objectMapper.readValue<Accept>(
                            httpRequestData.body.toByteArray().decodeToString()
                        )
                    )
                    respondOk()
                })
            )
        activityPubFollowService.receiveFollowJob(
            JobProps(
                data = mapOf<String, Any>(
                    ReceiveFollowJob.actor.name to "https://follower.example.com",
                    ReceiveFollowJob.targetActor.name to "https://example.com",
                    ReceiveFollowJob.follow.name to """{"type":"Follow","name":"Follow","object":"https://example.com","actor":"https://follower.example.com","@context":null}"""
                ),
                json = Json
            )
        )
    }
}
