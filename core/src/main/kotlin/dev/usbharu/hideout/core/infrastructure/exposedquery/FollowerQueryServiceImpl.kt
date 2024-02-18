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

package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.query.FollowerQueryService
import org.springframework.stereotype.Repository

@Repository
class FollowerQueryServiceImpl(
    private val relationshipRepository: RelationshipRepository,
    private val actorRepository: ActorRepository
) : FollowerQueryService {
    override suspend fun findFollowersById(id: Long): List<Actor> {
        return actorRepository.findByIds(
            relationshipRepository.findByTargetIdAndFollowing(id, true).map { it.actorId }
        )
    }

    override suspend fun alreadyFollow(actorId: Long, followerId: Long): Boolean =
        relationshipRepository.findByUserIdAndTargetUserId(followerId, actorId)?.following ?: false
}
