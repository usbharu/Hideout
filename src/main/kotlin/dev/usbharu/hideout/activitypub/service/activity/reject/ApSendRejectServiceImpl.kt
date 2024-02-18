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
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.external.job.DeliverRejectJob
import dev.usbharu.hideout.core.external.job.DeliverRejectJobParam
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.springframework.stereotype.Service

@Service
class ApSendRejectServiceImpl(
    private val applicationConfig: ApplicationConfig,
    private val jobQueueParentService: JobQueueParentService,
    private val deliverRejectJob: DeliverRejectJob
) : ApSendRejectService {
    override suspend fun sendRejectFollow(actor: Actor, target: Actor) {
        val deliverRejectJobParam = DeliverRejectJobParam(
            Reject(
                actor.url,
                "${applicationConfig.url}/reject/${actor.id}/${target.id}",
                Follow(apObject = actor.url, actor = target.url)
            ),
            target.inbox,
            actor.id
        )

        jobQueueParentService.scheduleTypeSafe(deliverRejectJob, deliverRejectJobParam)
    }
}
