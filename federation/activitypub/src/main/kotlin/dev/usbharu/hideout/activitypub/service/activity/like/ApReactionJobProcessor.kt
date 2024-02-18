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
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.DeliverReactionJob
import dev.usbharu.hideout.core.external.job.DeliverReactionJobParam
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Service

@Service
class ApReactionJobProcessor(
    private val apRequestService: APRequestService,
    private val applicationConfig: ApplicationConfig,
    private val transaction: Transaction,
    private val actorRepository: ActorRepository
) : JobProcessor<DeliverReactionJobParam, DeliverReactionJob> {
    override suspend fun process(param: DeliverReactionJobParam): Unit = transaction.transaction {
        val signer = actorRepository.findByUrl(param.actor)

        apRequestService.apPost(
            param.inbox,
            Like(
                actor = param.actor,
                apObject = param.postUrl,
                id = "${applicationConfig.url}/liek/note/${param.id}",
                content = param.reaction
            ),
            signer
        )
    }

    override fun job(): DeliverReactionJob = DeliverReactionJob
}
