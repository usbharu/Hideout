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

package dev.usbharu.hideout.activitypub.service.activity.undo

import dev.usbharu.hideout.activitypub.domain.model.Block
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Undo
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.external.job.DeliverUndoJob
import dev.usbharu.hideout.core.external.job.DeliverUndoJobParam
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class APSendUndoServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val deliverUndoJob: DeliverUndoJob,
    private val applicationConfig: ApplicationConfig
) : APSendUndoService {
    override suspend fun sendUndoFollow(actor: Actor, target: Actor) {
        val deliverUndoJobParam = DeliverUndoJobParam(
            Undo(
                actor = actor.url,
                id = "${applicationConfig.url}/undo/follow/${actor.id}/${target.url}",
                apObject = Follow(
                    apObject = actor.url,
                    actor = target.url
                ),
                published = Instant.now().toString()
            ),
            target.inbox,
            actor.id
        )

        jobQueueParentService.scheduleTypeSafe(deliverUndoJob, deliverUndoJobParam)
    }

    override suspend fun sendUndoBlock(actor: Actor, target: Actor) {
        val deliverUndoJobParam = DeliverUndoJobParam(
            Undo(
                actor = actor.url,
                id = "${applicationConfig.url}/undo/block/${actor.id}/${target.url}",
                apObject = Block(
                    apObject = actor.url,
                    actor = target.url,
                    id = "${applicationConfig.url}/block/${actor.id}/${target.id}"
                ),
                published = Instant.now().toString()
            ),
            target.inbox,
            actor.id
        )

        jobQueueParentService.scheduleTypeSafe(deliverUndoJob, deliverUndoJobParam)
    }
}
