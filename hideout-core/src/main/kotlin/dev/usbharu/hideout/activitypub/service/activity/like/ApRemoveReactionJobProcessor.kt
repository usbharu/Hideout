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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Like
import dev.usbharu.hideout.activitypub.domain.model.Undo
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.DeliverRemoveReactionJob
import dev.usbharu.hideout.core.external.job.DeliverRemoveReactionJobParam
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ApRemoveReactionJobProcessor(
    private val transaction: Transaction,
    private val objectMapper: ObjectMapper,
    private val apRequestService: APRequestService,
    private val applicationConfig: ApplicationConfig,
    private val actorRepository: ActorRepository
) : JobProcessor<DeliverRemoveReactionJobParam, DeliverRemoveReactionJob> {
    override suspend fun process(param: DeliverRemoveReactionJobParam): Unit = transaction.transaction {
        val like = objectMapper.readValue<Like>(param.like)

        val signer = actorRepository.findByUrl(param.actor)

        apRequestService.apPost(
            param.inbox,
            Undo(
                actor = param.actor,
                apObject = like,
                id = "${applicationConfig.url}/undo/like/${param.id}",
                published = Instant.now().toString()
            ),
            signer
        )
    }

    override fun job(): DeliverRemoveReactionJob = DeliverRemoveReactionJob
}
