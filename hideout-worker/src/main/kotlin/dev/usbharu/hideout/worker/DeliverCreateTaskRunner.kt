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
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.external.job.DeliverCreateTask
import dev.usbharu.hideout.core.external.job.DeliverCreateTaskDef
import dev.usbharu.owl.consumer.AbstractTaskRunner
import dev.usbharu.owl.consumer.TaskRequest
import dev.usbharu.owl.consumer.TaskResult
import org.springframework.stereotype.Component

@Component
class DeliverCreateTaskRunner(
    private val transaction: Transaction,
    private val apRequestService: APRequestService,
    private val actorRepository: ActorRepository,
) : AbstractTaskRunner<DeliverCreateTask, DeliverCreateTaskDef>(DeliverCreateTaskDef) {
    override suspend fun typedRun(typedParam: DeliverCreateTask, taskRequest: TaskRequest): TaskResult {
        transaction.transaction {
            val signer = actorRepository.findByUrl(typedParam.actor)

            apRequestService.apPost(typedParam.inbox, typedParam.create, signer)
        }

        return TaskResult.ok()
    }
}