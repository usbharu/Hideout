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

package dev.usbharu.hideout.worker

import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.DeliverRejectTask
import dev.usbharu.hideout.core.external.job.DeliverRejectTaskDef
import dev.usbharu.owl.consumer.AbstractTaskRunner
import dev.usbharu.owl.consumer.TaskRequest
import dev.usbharu.owl.consumer.TaskResult
import org.springframework.stereotype.Component

@Component
class DeliverRejectTaskRunner(
    private val transaction: Transaction,
    private val apRequestService: APRequestService,
    private val actorRepository: ActorRepository,
) : AbstractTaskRunner<DeliverRejectTask, DeliverRejectTaskDef>(DeliverRejectTaskDef) {
    override suspend fun typedRun(typedParam: DeliverRejectTask, taskRequest: TaskRequest): TaskResult {
        val signer = transaction.transaction {
            actorRepository.findById(typedParam.signer)
        }
        apRequestService.apPost(typedParam.inbox, typedParam.reject, signer)

        return TaskResult.ok()
    }
}