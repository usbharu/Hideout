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

import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.external.job.ReceiveFollowTask
import dev.usbharu.hideout.core.external.job.ReceiveFollowTaskDef
import dev.usbharu.owl.consumer.AbstractTaskRunner
import dev.usbharu.owl.consumer.TaskRequest
import dev.usbharu.owl.consumer.TaskResult
import org.springframework.stereotype.Component

@Component
class ReceiveFollowTaskRunner(
    private val transaction: Transaction,
    private val apUserService: APUserService,
    private val actorRepository: ActorRepository,
    private val relationshipService: RelationshipService,
) : AbstractTaskRunner<ReceiveFollowTask, ReceiveFollowTaskDef>(ReceiveFollowTaskDef) {
    override suspend fun typedRun(typedParam: ReceiveFollowTask, taskRequest: TaskRequest): TaskResult {

        transaction.transaction {

            apUserService.fetchPerson(typedParam.actor, typedParam.targetActor)
            val targetEntity = actorRepository.findByUrl(typedParam.targetActor) ?: throw UserNotFoundException.withUrl(
                typedParam.targetActor
            )
            val followActorEntity = actorRepository.findByUrl(typedParam.follow.actor)
                ?: throw UserNotFoundException.withUrl(typedParam.follow.actor)
            relationshipService.followRequest(followActorEntity.id, targetEntity.id)
        }

        return TaskResult.ok()
    }
}