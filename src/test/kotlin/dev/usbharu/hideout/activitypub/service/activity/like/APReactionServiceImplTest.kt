/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.activitypub.service.activity.like


import dev.usbharu.hideout.application.service.id.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.external.job.DeliverReactionJob
import dev.usbharu.hideout.core.external.job.DeliverRemoveReactionJob
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.service.job.JobQueueParentService
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

        val postQueryService = mock<PostRepository> {
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
        val actorRepository = mock<ActorRepository> {
            onBlocking { findById(eq(user.id)) }.doReturn(user)
        }
        val apReactionServiceImpl = APReactionServiceImpl(
            jobQueueParentService = jobQueueParentService,
            actorRepository = actorRepository,
            followerQueryService = followerQueryService,
            postRepository = postQueryService,
            objectMapper = objectMapper
        )

        apReactionServiceImpl.reaction(
            Reaction(
                id = TwitterSnowflakeIdGenerateService.generateId(),
                emoji = UnicodeEmoji("❤"),
                postId = post.id,
                actorId = user.id
            )
        )

        verify(jobQueueParentService, times(3)).schedule(eq(DeliverReactionJob), any())
    }

    @Test
    fun `removeReaction リアクションを削除するとフォロワーの数だけ配送ジョブが作成される`() = runTest {

        val user = UserBuilder.localUserOf()
        val post = PostBuilder.of()

        val postQueryService = mock<PostRepository> {
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
        val actorRepository = mock<ActorRepository> {
            onBlocking { findById(eq(user.id)) }.doReturn(user)
        }
        val apReactionServiceImpl = APReactionServiceImpl(
            jobQueueParentService = jobQueueParentService,
            actorRepository = actorRepository,
            followerQueryService = followerQueryService,
            postRepository = postQueryService,
            objectMapper = objectMapper
        )

        apReactionServiceImpl.removeReaction(
            Reaction(
                id = TwitterSnowflakeIdGenerateService.generateId(),
                emoji = UnicodeEmoji("❤"),
                postId = post.id,
                actorId = user.id
            )
        )

        verify(jobQueueParentService, times(3)).schedule(eq(DeliverRemoveReactionJob), any())
    }
}
