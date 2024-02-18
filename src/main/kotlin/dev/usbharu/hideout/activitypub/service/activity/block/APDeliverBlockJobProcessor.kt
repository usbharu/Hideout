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

package dev.usbharu.hideout.activitypub.service.activity.block

import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.DeliverBlockJob
import dev.usbharu.hideout.core.external.job.DeliverBlockJobParam
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Service

/**
 * ブロックアクティビティ配送を処理します
 */
@Service
class APDeliverBlockJobProcessor(
    private val apRequestService: APRequestService,
    private val actorRepository: ActorRepository,
    private val transaction: Transaction,
    private val deliverBlockJob: DeliverBlockJob
) : JobProcessor<DeliverBlockJobParam, DeliverBlockJob> {
    override suspend fun process(param: DeliverBlockJobParam): Unit = transaction.transaction {
        val signer = actorRepository.findById(param.signer)
        apRequestService.apPost(
            param.inbox,
            param.reject,
            signer
        )
        apRequestService.apPost(
            param.inbox,
            param.block,
            signer
        )
    }

    override fun job(): DeliverBlockJob = deliverBlockJob
}
