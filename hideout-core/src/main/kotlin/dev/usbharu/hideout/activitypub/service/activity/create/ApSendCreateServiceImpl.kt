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

package dev.usbharu.hideout.activitypub.service.activity.create

import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.resource.PostNotFoundException
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.external.job.DeliverCreateTask
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.owl.producer.api.OwlProducer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ApSendCreateServiceImpl(
    private val followerQueryService: FollowerQueryService,
    private val noteQueryService: NoteQueryService,
    private val applicationConfig: ApplicationConfig,
    private val actorRepository: ActorRepository,
    private val owlProducer: OwlProducer,
) : ApSendCreateService {
    override suspend fun createNote(post: Post) {
        logger.info("CREATE Create Local Note ${post.url}")
        logger.debug("START Create Local Note ${post.url}")
        logger.trace("{}", post)
        val followers = followerQueryService.findFollowersById(post.actorId)

        logger.debug("DELIVER Deliver Note Create ${followers.size} accounts.")

        val userEntity = actorRepository.findById(post.actorId) ?: throw UserNotFoundException.withId(post.actorId)
        val note = noteQueryService.findById(post.id)?.first ?: throw PostNotFoundException.withId(post.id)
        val create = Create(
            name = "Create Note",
            apObject = note,
            actor = note.attributedTo,
            id = "${applicationConfig.url}/create/note/${post.id}"
        )
        followers.forEach { followerEntity ->
            owlProducer.publishTask(DeliverCreateTask(create, userEntity.url, followerEntity.inbox))
        }

        logger.debug("SUCCESS Create Local Note ${post.url}")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ApSendCreateServiceImpl::class.java)
    }
}
