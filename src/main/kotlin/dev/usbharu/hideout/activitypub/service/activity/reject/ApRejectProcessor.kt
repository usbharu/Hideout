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

package dev.usbharu.hideout.activitypub.service.activity.reject

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Reject
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import org.springframework.stereotype.Service

@Service
class ApRejectProcessor(
    private val relationshipService: RelationshipService,
    transaction: Transaction,
    private val actorRepository: ActorRepository
) :
    AbstractActivityPubProcessor<Reject>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Reject>) {
        val activityType = activity.activity.apObject.type.firstOrNull { it == "Follow" }

        if (activityType == null) {
            logger.warn("FAILED Process Reject Activity type: {}", activity.activity.apObject.type)
            return
        }
        when (activityType) {
            "Follow" -> {
                val user = actorRepository.findByUrl(activity.activity.actor) ?: throw UserNotFoundException.withUrl(
                    activity.activity.actor
                )

                activity.activity.apObject as Follow

                val actor = activity.activity.apObject.actor

                val target = actorRepository.findByUrl(actor) ?: throw UserNotFoundException.withUrl(actor)

                logger.debug("REJECT Follow user {} target {}", user.url, target.url)

                relationshipService.rejectFollowRequest(user.id, target.id)
            }

            else -> {}
        }
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Reject

    override fun type(): Class<Reject> = Reject::class.java
}
