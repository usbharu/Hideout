package dev.usbharu.hideout.service.ap


import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.domain.model.job.DeliverReactionJob
import dev.usbharu.hideout.domain.model.job.DeliverRemoveReactionJob
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.PostQueryService
import dev.usbharu.hideout.service.core.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.service.job.JobQueueParentService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import utils.JsonObjectMapper.objectMapper
import utils.PostBuilder
import utils.UserBuilder

class APReactionServiceImplTest {
    @Test
    fun `reaction リアクションするとフォロワーの数だけ配送ジョブが作成される`() = runTest {

        val user = UserBuilder.localUserOf()
        val post = PostBuilder.of()

        val postQueryService = mock<PostQueryService> {
            onBlocking { findById(eq(post.id)) } doReturn post
        }
        val followerQueryService = mock<FollowerQueryService> {
            onBlocking { findFollowersById(eq(user.id)) } doReturn listOf(
                UserBuilder.localUserOf(),
                UserBuilder.localUserOf(),
                UserBuilder.localUserOf()
            )
        }
        val jobQueueParentService = mock<JobQueueParentService>()
        val apReactionServiceImpl = APReactionServiceImpl(
            jobQueueParentService = jobQueueParentService,
            userQueryService = mock(),
            followerQueryService = followerQueryService,
            postQueryService = postQueryService,
            objectMapper = objectMapper
        )

        apReactionServiceImpl.reaction(
            Reaction(
                id = TwitterSnowflakeIdGenerateService.generateId(),
                emojiId = 0,
                postId = post.id,
                userId = user.id
            )
        )

        verify(jobQueueParentService, times(3)).schedule(eq(DeliverReactionJob), any())
    }

    @Test
    fun `removeReaction リアクションを削除するとフォロワーの数だけ配送ジョブが作成される`() = runTest {

        val user = UserBuilder.localUserOf()
        val post = PostBuilder.of()

        val postQueryService = mock<PostQueryService> {
            onBlocking { findById(eq(post.id)) } doReturn post
        }
        val followerQueryService = mock<FollowerQueryService> {
            onBlocking { findFollowersById(eq(user.id)) } doReturn listOf(
                UserBuilder.localUserOf(),
                UserBuilder.localUserOf(),
                UserBuilder.localUserOf()
            )
        }
        val jobQueueParentService = mock<JobQueueParentService>()
        val apReactionServiceImpl = APReactionServiceImpl(
            jobQueueParentService = jobQueueParentService,
            userQueryService = mock(),
            followerQueryService = followerQueryService,
            postQueryService = postQueryService,
            objectMapper = objectMapper
        )

        apReactionServiceImpl.removeReaction(
            Reaction(
                id = TwitterSnowflakeIdGenerateService.generateId(),
                emojiId = 0,
                postId = post.id,
                userId = user.id
            )
        )

        verify(jobQueueParentService, times(3)).schedule(eq(DeliverRemoveReactionJob), any())
    }
}
