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

package dev.usbharu.hideout.activitypub.service.activity.delete

import dev.usbharu.hideout.activitypub.domain.model.Delete
import dev.usbharu.hideout.activitypub.domain.model.Tombstone
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectValue
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.external.job.DeliverDeleteTask
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.owl.producer.api.OwlProducer
import org.springframework.stereotype.Service
import java.time.Instant

interface APSendDeleteService {
    suspend fun sendDeleteNote(deletedPost: Post)
    suspend fun sendDeleteActor(deletedActor: Actor)
}

@Service
class APSendDeleteServiceImpl(
    private val followerQueryService: FollowerQueryService,
    private val applicationConfig: ApplicationConfig,
    private val actorRepository: ActorRepository,
    private val owlProducer: OwlProducer,
) : APSendDeleteService {
    override suspend fun sendDeleteNote(deletedPost: Post) {
        val actor =
            actorRepository.findById(deletedPost.actorId) ?: throw UserNotFoundException.withId(deletedPost.actorId)
        val followersById = followerQueryService.findFollowersById(deletedPost.actorId)

        val delete = Delete(
            actor = actor.url,
            id = "${applicationConfig.url}/delete/note/${deletedPost.id}",
            published = Instant.now().toString(),
            `object` = Tombstone(id = deletedPost.apId)
        )

        followersById.forEach {
            val jobProps = DeliverDeleteTask(
                delete,
                it.inbox,
                actor.id
            )

            owlProducer.publishTask(jobProps)
        }
    }

    override suspend fun sendDeleteActor(deletedActor: Actor) {
        val followers = followerQueryService.findFollowersById(deletedActor.id)

        val delete = Delete(
            actor = deletedActor.url,
            `object` = ObjectValue(emptyList(), `object` = deletedActor.url),
            id = "${applicationConfig.url}/delete/actor/${deletedActor.id}",
            published = Instant.now().toString()
        )

        followers.forEach {
            DeliverDeleteTask(
                delete = delete,
                it.inbox,
                deletedActor.id
            )
        }
    }
}
