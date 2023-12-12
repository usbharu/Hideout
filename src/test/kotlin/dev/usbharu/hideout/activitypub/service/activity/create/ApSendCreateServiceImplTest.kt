package dev.usbharu.hideout.activitypub.service.activity.create

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteServiceImpl
import dev.usbharu.hideout.application.config.ActivityPubConfig
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.external.job.DeliverPostJob
import dev.usbharu.hideout.core.query.ActorQueryService
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
    private lateinit var actorQueryService: ActorQueryService

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
        whenever(actorQueryService.findById(eq(post.actorId))).doReturn(user)
        whenever(noteQueryService.findById(eq(post.id))).doReturn(note to post)

        apSendCreateServiceImpl.createNote(post)

        verify(jobQueueParentService, times(followers.size)).schedule(eq(DeliverPostJob), any())
    }
}
