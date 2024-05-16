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

import dev.usbharu.hideout.activitypub.domain.model.Like
import dev.usbharu.hideout.activitypub.domain.model.Undo
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.resource.PostNotFoundException
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.external.job.DeliverReactionTask
import dev.usbharu.hideout.core.external.job.DeliverUndoTask
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.owl.producer.api.OwlProducer
import org.springframework.stereotype.Service
import java.time.Instant

interface APReactionService {
    suspend fun reaction(like: Reaction)
    suspend fun removeReaction(like: Reaction)
}

@Service
class APReactionServiceImpl(
    private val followerQueryService: FollowerQueryService,
    private val actorRepository: ActorRepository,
    private val postRepository: PostRepository,
    private val applicationConfig: ApplicationConfig,
    private val owlProducer: OwlProducer,
) : APReactionService {
    override suspend fun reaction(like: Reaction) {
        val followers = followerQueryService.findFollowersById(like.actorId)
        val user = actorRepository.findById(like.actorId) ?: throw UserNotFoundException.withId(like.actorId)
        val post =
            postRepository.findById(like.postId) ?: throw PostNotFoundException.withId(like.postId)
        followers.forEach { follower ->
            owlProducer.publishTask(
                DeliverReactionTask(
                    actor = user.url,
                    like = Like(
                        actor = user.url,
                        id = "${applicationConfig.url}/like/note/${post.id}",
                        content = "❤",
                        apObject = post.url
                    ),
                    inbox = follower.inbox
                )
            )
        }
    }

    override suspend fun removeReaction(like: Reaction) {
        val followers = followerQueryService.findFollowersById(like.actorId)
        val user = actorRepository.findById(like.actorId) ?: throw UserNotFoundException.withId(like.actorId)
        val post =
            postRepository.findById(like.postId) ?: throw PostNotFoundException.withId(like.postId)
        followers.forEach { follower ->
            owlProducer.publishTask(
                DeliverUndoTask(
                    signer = user.id,
                    inbox = follower.inbox,
                    undo = Undo(
                        actor = user.url,
                        id = "${applicationConfig.url}/undo/like/${post.id}",
                        apObject = Like(
                            actor = user.url,
                            id = "${applicationConfig.url}/like/note/${post.id}",
                            content = "❤",
                            apObject = post.url
                        ),
                        published = Instant.now().toString(),
                    )
                )
            )
        }
    }
}
