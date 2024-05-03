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

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.core.domain.exception.resource.PostNotFoundException
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.external.job.DeliverReactionJob
import dev.usbharu.hideout.core.external.job.DeliverRemoveReactionJob
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface APReactionService {
    suspend fun reaction(like: Reaction)
    suspend fun removeReaction(like: Reaction)
}

@Service
class APReactionServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val followerQueryService: FollowerQueryService,
    private val actorRepository: ActorRepository,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val postRepository: PostRepository
) : APReactionService {
    override suspend fun reaction(like: Reaction) {
        val followers = followerQueryService.findFollowersById(like.actorId)
        val user = actorRepository.findById(like.actorId) ?: throw UserNotFoundException.withId(like.actorId)
        val post =
            postRepository.findById(like.postId) ?: throw PostNotFoundException.withId(like.postId)
        followers.forEach { follower ->
            jobQueueParentService.schedule(DeliverReactionJob) {
                props[DeliverReactionJob.actor] = user.url
                props[DeliverReactionJob.reaction] = "❤"
                props[DeliverReactionJob.inbox] = follower.inbox
                props[DeliverReactionJob.postUrl] = post.url
                props[DeliverReactionJob.id] = post.id.toString()
            }
        }
    }

    override suspend fun removeReaction(like: Reaction) {
        val followers = followerQueryService.findFollowersById(like.actorId)
        val user = actorRepository.findById(like.actorId) ?: throw UserNotFoundException.withId(like.actorId)
        val post =
            postRepository.findById(like.postId) ?: throw PostNotFoundException.withId(like.postId)
        followers.forEach { follower ->
            jobQueueParentService.schedule(DeliverRemoveReactionJob) {
                props[DeliverRemoveReactionJob.actor] = user.url
                props[DeliverRemoveReactionJob.inbox] = follower.inbox
                props[DeliverRemoveReactionJob.id] = post.id.toString()
                props[DeliverRemoveReactionJob.like] = objectMapper.writeValueAsString(like)
            }
        }
    }
}
