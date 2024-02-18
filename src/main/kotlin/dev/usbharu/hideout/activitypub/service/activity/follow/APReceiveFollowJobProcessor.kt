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

package dev.usbharu.hideout.activitypub.service.activity.follow

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.ReceiveFollowJob
import dev.usbharu.hideout.core.external.job.ReceiveFollowJobParam
import dev.usbharu.hideout.core.service.job.JobProcessor
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class APReceiveFollowJobProcessor(
    private val transaction: Transaction,
    private val apUserService: APUserService,
    private val objectMapper: ObjectMapper,
    private val relationshipService: RelationshipService,
    private val actorRepository: ActorRepository
) :
    JobProcessor<ReceiveFollowJobParam, ReceiveFollowJob> {
    override suspend fun process(param: ReceiveFollowJobParam) = transaction.transaction {
        apUserService.fetchPerson(param.actor, param.targetActor)
        val follow = objectMapper.readValue<Follow>(param.follow)

        logger.info("START Follow from: {} to {}", param.targetActor, param.actor)

        val targetEntity =
            actorRepository.findByUrl(param.targetActor) ?: throw UserNotFoundException.withUrl(param.targetActor)
        val followActorEntity =
            actorRepository.findByUrl(follow.actor) ?: throw UserNotFoundException.withUrl(follow.actor)

        relationshipService.followRequest(followActorEntity.id, targetEntity.id)

        logger.info("SUCCESS Follow from: {} to: {}", param.targetActor, param.actor)
    }

    override fun job(): ReceiveFollowJob = ReceiveFollowJob

    companion object {
        private val logger = LoggerFactory.getLogger(APReceiveFollowJobProcessor::class.java)
    }
}
