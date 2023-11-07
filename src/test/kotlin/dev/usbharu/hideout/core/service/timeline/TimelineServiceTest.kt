package dev.usbharu.hideout.core.service.timeline

import dev.usbharu.hideout.application.service.id.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.query.UserQueryService
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.PostBuilder
import utils.UserBuilder

@ExtendWith(MockitoExtension::class)
class TimelineServiceTest {

    @Mock
    private lateinit var followerQueryService: FollowerQueryService

    @Mock
    private lateinit var userQueryService: UserQueryService

    @Mock
    private lateinit var timelineRepository: TimelineRepository

    @InjectMocks
    private lateinit var timelineService: TimelineService

    @Captor
    private lateinit var captor: ArgumentCaptor<List<Timeline>>

    @Test
    fun `publishTimeline ローカルの投稿はローカルのフォロワーと投稿者のタイムラインに追加される`() = runTest {
        val post = PostBuilder.of()
        val listOf = listOf<User>(UserBuilder.localUserOf(), UserBuilder.localUserOf())
        val localUserOf = UserBuilder.localUserOf(id = post.userId)

        whenever(followerQueryService.findFollowersById(eq(post.userId))).doReturn(listOf)
        whenever(userQueryService.findById(eq(post.userId))).doReturn(localUserOf)
        whenever(timelineRepository.generateId()).doReturn(TwitterSnowflakeIdGenerateService.generateId())


        timelineService.publishTimeline(post, true)

        verify(timelineRepository).saveAll(capture(captor))
        val timelineList = captor.value

        assertThat(timelineList).hasSize(4).anyMatch { it.userId == post.userId }
    }

    @Test
    fun `publishTimeline リモートの投稿はローカルのフォロワーのタイムラインに追加される`() = runTest {
        val post = PostBuilder.of()
        val listOf = listOf<User>(UserBuilder.localUserOf(), UserBuilder.localUserOf())

        whenever(followerQueryService.findFollowersById(eq(post.userId))).doReturn(listOf)
        whenever(timelineRepository.generateId()).doReturn(TwitterSnowflakeIdGenerateService.generateId())


        timelineService.publishTimeline(post, false)

        verify(timelineRepository).saveAll(capture(captor))
        val timelineList = captor.value

        assertThat(timelineList).hasSize(3)
    }

    @Test
    fun `publishTimeline パブリック投稿はパブリックタイムラインにも追加される`() = runTest {
        val post = PostBuilder.of()
        val listOf = listOf<User>(UserBuilder.localUserOf(), UserBuilder.localUserOf())

        whenever(followerQueryService.findFollowersById(eq(post.userId))).doReturn(listOf)
        whenever(timelineRepository.generateId()).doReturn(TwitterSnowflakeIdGenerateService.generateId())


        timelineService.publishTimeline(post, false)

        verify(timelineRepository).saveAll(capture(captor))
        val timelineList = captor.value

        assertThat(timelineList).hasSize(3).anyMatch { it.userId == 0L }
    }

    @Test
    fun `publishTimeline パブリック投稿ではない場合はローカルのフォロワーのみに追加される`() = runTest {
        val post = PostBuilder.of(visibility = Visibility.UNLISTED)
        val listOf = listOf<User>(UserBuilder.localUserOf(), UserBuilder.localUserOf())

        whenever(followerQueryService.findFollowersById(eq(post.userId))).doReturn(listOf)
        whenever(timelineRepository.generateId()).doReturn(TwitterSnowflakeIdGenerateService.generateId())


        timelineService.publishTimeline(post, false)

        verify(timelineRepository).saveAll(capture(captor))
        val timelineList = captor.value

        assertThat(timelineList).hasSize(2).noneMatch { it.userId == 0L }
    }
}
