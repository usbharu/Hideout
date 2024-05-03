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

import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.DeliverRejectJob
import dev.usbharu.hideout.core.external.job.DeliverRejectJobParam
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Component

@Component
class APDeliverRejectJobProcessor(
    private val apRequestService: APRequestService,
    private val deliverRejectJob: DeliverRejectJob,
    private val transaction: Transaction,
    private val actorRepository: ActorRepository
) :
    JobProcessor<DeliverRejectJobParam, DeliverRejectJob> {
    override suspend fun process(param: DeliverRejectJobParam): Unit = transaction.transaction {
        apRequestService.apPost(param.inbox, param.reject, actorRepository.findById(param.signer))
    }

    override fun job(): DeliverRejectJob = deliverRejectJob
}
