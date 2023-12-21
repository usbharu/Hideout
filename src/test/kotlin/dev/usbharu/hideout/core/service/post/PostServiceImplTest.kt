package dev.usbharu.hideout.core.service.post

import dev.usbharu.hideout.activitypub.service.activity.create.ApSendCreateService
import dev.usbharu.hideout.application.config.CharacterLimit
import dev.usbharu.hideout.core.domain.exception.resource.DuplicateException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import dev.usbharu.hideout.core.service.timeline.TimelineService
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mockStatic
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.PostBuilder
import utils.UserBuilder
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class PostServiceImplTest {

    @Mock
    private lateinit var postRepository: PostRepository

    @Mock
    private lateinit var actorRepository: ActorRepository

    @Mock
    private lateinit var timelineService: TimelineService
    @Spy
    private var postBuilder: Post.PostBuilder = Post.PostBuilder(CharacterLimit())

    @Mock
    private lateinit var apSendCreateService: ApSendCreateService

    @Mock
    private lateinit var reactionRepository: ReactionRepository

    @InjectMocks
    private lateinit var postServiceImpl: PostServiceImpl

    @Test
    fun `createLocal 正常にpostを作成できる`() = runTest {

        val now = Instant.now()
        val post = PostBuilder.of(createdAt = now.toEpochMilli())

        whenever(postRepository.save(eq(post))).doReturn(post)
        whenever(postRepository.generateId()).doReturn(post.id)
        whenever(actorRepository.findById(eq(post.actorId))).doReturn(UserBuilder.localUserOf(id = post.actorId))
        whenever(timelineService.publishTimeline(eq(post), eq(true))).doReturn(Unit)

        mockStatic(Instant::class.java, Mockito.CALLS_REAL_METHODS).use {

            it.`when`<Instant>(Instant::now).doReturn(now)
            val createLocal = postServiceImpl.createLocal(
                PostCreateDto(
                    post.text,
                    post.overview,
                    post.visibility,
                    post.repostId,
                    post.replyId,
                    post.actorId,
                    post.mediaIds
                )
            )

            assertThat(createLocal).isEqualTo(post)
        }

        verify(postRepository, times(1)).save(eq(post))
        verify(timelineService, times(1)).publishTimeline(eq(post), eq(true))
        verify(apSendCreateService, times(1)).createNote(eq(post))
    }

    @Test
    fun `createRemote 正常にリモートのpostを作成できる`() = runTest {
        val post = PostBuilder.of()

        whenever(actorRepository.findById(eq(post.actorId))).doReturn(UserBuilder.remoteUserOf(id = post.actorId))
        whenever(postRepository.save(eq(post))).doReturn(post)
        whenever(timelineService.publishTimeline(eq(post), eq(false))).doReturn(Unit)


        val createLocal = postServiceImpl.createRemote(post)

        assertThat(createLocal).isEqualTo(post)


        verify(postRepository, times(1)).save(eq(post))
        verify(timelineService, times(1)).publishTimeline(eq(post), eq(false))
    }

    @Test
    fun `createRemote 既に作成されていた場合はそのまま帰す`() = runTest {
        val post = PostBuilder.of()

        whenever(actorRepository.findById(eq(post.actorId))).doReturn(UserBuilder.remoteUserOf(id = post.actorId))
        whenever(postRepository.save(eq(post))).doAnswer { throw DuplicateException() }
        whenever(postRepository.findByApId(eq(post.apId))).doReturn(post)

        val createLocal = postServiceImpl.createRemote(post)

        assertThat(createLocal).isEqualTo(post)

        verify(postRepository, times(1)).save(eq(post))
        verify(timelineService, times(0)).publishTimeline(any(), any())
    }

    @Test
    fun `createRemote 既に作成されていることを検知出来ずタイムラインにpush出来なかった場合何もしない`() = runTest {
        val post = PostBuilder.of()

        whenever(actorRepository.findById(eq(post.actorId))).doReturn(UserBuilder.remoteUserOf(id = post.actorId))
        whenever(postRepository.save(eq(post))).doReturn(post)
        whenever(timelineService.publishTimeline(eq(post), eq(false))).doThrow(DuplicateException::class)
        whenever(postRepository.findByApId(eq(post.apId))).doReturn(post)

        val createLocal = postServiceImpl.createRemote(post)

        assertThat(createLocal).isEqualTo(post)

        verify(postRepository, times(1)).save(eq(post))
        verify(timelineService, times(1)).publishTimeline(eq(post), eq(false))
    }
}
