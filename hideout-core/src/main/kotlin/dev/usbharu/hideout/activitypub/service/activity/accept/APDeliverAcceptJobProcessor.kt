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

import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.DeliverAcceptJob
import dev.usbharu.hideout.core.external.job.DeliverAcceptJobParam
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Service

@Service
class APDeliverAcceptJobProcessor(
    private val apRequestService: APRequestService,
    private val deliverAcceptJob: DeliverAcceptJob,
    private val transaction: Transaction,
    private val actorRepository: ActorRepository
) :
    JobProcessor<DeliverAcceptJobParam, DeliverAcceptJob> {
    override suspend fun process(param: DeliverAcceptJobParam): Unit = transaction.transaction {
        apRequestService.apPost(param.inbox, param.accept, actorRepository.findById(param.signer))
    }

    override fun job(): DeliverAcceptJob = deliverAcceptJob
}
