@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.usbharu.hideout.service.ap.job

import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.domain.model.ap.Like
import dev.usbharu.hideout.domain.model.ap.Undo
import dev.usbharu.hideout.domain.model.job.DeliverReactionJob
import dev.usbharu.hideout.domain.model.job.DeliverRemoveReactionJob
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.ap.APRequestService
import kjob.core.job.JobProps
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.*
import utils.JsonObjectMapper.objectMapper
import utils.UserBuilder
import java.net.URL
import java.time.Instant

class ApReactionJobServiceImplTest {
    @Test
    fun `reactionJob Likeが配送される`() = runTest {

        val localUser = UserBuilder.localUserOf()
        val remoteUser = UserBuilder.remoteUserOf()

        val userQueryService = mock<UserQueryService> {
            onBlocking { findByUrl(localUser.url) } doReturn localUser
        }
        val apRequestService = mock<APRequestService>()
        val apReactionJobServiceImpl = ApReactionJobServiceImpl(
            userQueryService = userQueryService,
            apRequestService = apRequestService,
            applicationConfig = ApplicationConfig(URL("https://example.com")),
            objectMapper = objectMapper
        )


        val postUrl = "${remoteUser.url}/posts/1234"

        apReactionJobServiceImpl.reactionJob(
            JobProps(
                data = mapOf(
                    DeliverReactionJob.inbox.name to remoteUser.inbox,
                    DeliverReactionJob.actor.name to localUser.url,
                    DeliverReactionJob.postUrl.name to postUrl,
                    DeliverReactionJob.id.name to "1234",
                    DeliverReactionJob.reaction.name to "❤",

                    ),
                json = Json
            )
        )

        val body = Like(
            name = "Like",
            actor = localUser.url,
            `object` = postUrl,
            id = "https://example.com/like/note/1234",
            content = "❤"
        )

        verify(apRequestService, times(1)).apPost(eq(remoteUser.inbox), eq(body), eq(localUser))

    }

    @Test
    fun `removeReactionJob LikeのUndoが配送される`() = runTest {

        val localUser = UserBuilder.localUserOf()
        val remoteUser = UserBuilder.remoteUserOf()

        val userQueryService = mock<UserQueryService> {
            onBlocking { findByUrl(localUser.url) } doReturn localUser
        }
        val apRequestService = mock<APRequestService>()
        val apReactionJobServiceImpl = ApReactionJobServiceImpl(
            userQueryService = userQueryService,
            apRequestService = apRequestService,
            applicationConfig = ApplicationConfig(URL("https://example.com")),
            objectMapper = objectMapper
        )


        val postUrl = "${remoteUser.url}/posts/1234"
        val like = Like(
            name = "Like",
            actor = remoteUser.url,
            `object` = postUrl,
            id = "https://example.com/like/note/1234",
            content = "❤"
        )

        val now = Instant.now()

        val body = mockStatic(Instant::class.java).use {

            it.`when`<Instant>(Instant::now).thenReturn(now)

            apReactionJobServiceImpl.removeReactionJob(
                JobProps(
                    data = mapOf(
                        DeliverRemoveReactionJob.inbox.name to remoteUser.inbox,
                        DeliverRemoveReactionJob.actor.name to localUser.url,
                        DeliverRemoveReactionJob.id.name to "1234",
                        DeliverRemoveReactionJob.like.name to objectMapper.writeValueAsString(like),

                        ),
                    json = Json
                )
            )
            Undo(
                name = "Undo Reaction",
                actor = localUser.url,
                `object` = like,
                id = "https://example.com/undo/note/1234",
                published = now
            )
        }



        verify(apRequestService, times(1)).apPost(eq(remoteUser.inbox), eq(body), eq(localUser))
    }
}
