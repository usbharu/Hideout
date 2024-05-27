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

package dev.usbharu.hideout.core.usecase.actor

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.Actor2Repository
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import org.springframework.stereotype.Service

@Service
class SuspendLocalActorApplicationService(
    private val transaction: Transaction,
    private val actor2Repository: Actor2Repository,
) {
    suspend fun suspend(actorId: Long, executor: ActorId) {
        transaction.transaction {

            val id = ActorId(actorId)

            val findById = actor2Repository.findById(id)!!
            findById.suspend = true
        }


    }
}