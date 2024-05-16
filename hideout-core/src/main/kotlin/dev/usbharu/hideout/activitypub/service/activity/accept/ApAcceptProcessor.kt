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

package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import org.springframework.stereotype.Service

@Service
class ApAcceptProcessor(
    transaction: Transaction,
    private val relationshipService: RelationshipService,
    private val actorRepository: ActorRepository
) :
    AbstractActivityPubProcessor<Accept>(transaction) {

    override suspend fun internalProcess(activity: ActivityPubProcessContext<Accept>) {
        val value = activity.activity.apObject

        if (value.type.contains("Follow").not()) {
            logger.warn("FAILED Activity type isn't Follow.")
            throw IllegalActivityPubObjectException("Invalid type ${value.type}")
        }

        val follow = value as Follow

        val userUrl = follow.apObject
        val followerUrl = follow.actor

        val user = actorRepository.findByUrl(userUrl) ?: throw UserNotFoundException.withUrl(userUrl)
        val follower = actorRepository.findByUrl(followerUrl) ?: throw UserNotFoundException.withUrl(followerUrl)

        relationshipService.acceptFollowRequest(user.id, follower.id)
        logger.debug("SUCCESS Follow from ${user.url} to ${follower.url}.")
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Accept

    override fun type(): Class<Accept> = Accept::class.java
}
