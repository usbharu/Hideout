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

package dev.usbharu.hideout.activitypub.service.objects.note

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.DeliverPostJob
import dev.usbharu.hideout.core.external.job.DeliverPostJobParam
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ApNoteJobProcessor(
    private val transaction: Transaction,
    private val objectMapper: ObjectMapper,
    private val apRequestService: APRequestService,
    private val actorRepository: ActorRepository
) : JobProcessor<DeliverPostJobParam, DeliverPostJob> {
    override suspend fun process(param: DeliverPostJobParam) {
        val create = objectMapper.readValue<Create>(param.create)
        transaction.transaction {
            val signer = actorRepository.findByUrl(param.actor)

            logger.debug("CreateNoteJob: actor: {} create: {} inbox: {}", param.actor, create, param.inbox)

            apRequestService.apPost(
                param.inbox,
                create,
                signer
            )
        }
    }

    override fun job(): DeliverPostJob = DeliverPostJob

    companion object {
        private val logger = LoggerFactory.getLogger(ApNoteJobProcessor::class.java)
    }
}
