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

import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.external.job.DeliverAcceptJob
import dev.usbharu.hideout.core.external.job.DeliverAcceptJobParam
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.springframework.stereotype.Service

interface ApSendAcceptService {
    suspend fun sendAcceptFollow(actor: Actor, target: Actor)
}

@Service
class ApSendAcceptServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val deliverAcceptJob: DeliverAcceptJob
) : ApSendAcceptService {
    override suspend fun sendAcceptFollow(actor: Actor, target: Actor) {
        val deliverAcceptJobParam = DeliverAcceptJobParam(
            Accept(
                apObject = Follow(
                    apObject = actor.url,
                    actor = target.url
                ),
                actor = actor.url
            ),
            target.inbox,
            actor.id
        )

        jobQueueParentService.scheduleTypeSafe(deliverAcceptJob, deliverAcceptJobParam)
    }
}
