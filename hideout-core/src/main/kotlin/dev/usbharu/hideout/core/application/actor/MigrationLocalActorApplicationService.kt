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

package dev.usbharu.hideout.core.application.actor

import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.service.actor.local.AccountMigrationCheck.*
import dev.usbharu.hideout.core.domain.service.actor.local.LocalActorMigrationCheckDomainService
import org.springframework.stereotype.Service

@Service
class MigrationLocalActorApplicationService(
    private val transaction: Transaction,
    private val actorRepository: ActorRepository,
    private val localActorMigrationCheckDomainService: LocalActorMigrationCheckDomainService,
) {
    suspend fun migration(from: Long, to: Long, executor: ActorId) {
        transaction.transaction<Unit> {
            val fromActorId = ActorId(from)
            val toActorId = ActorId(to)

            val fromActor = actorRepository.findById(fromActorId)!!
            val toActor = actorRepository.findById(toActorId)!!

            val canAccountMigration = localActorMigrationCheckDomainService.canAccountMigration(fromActor, toActor)
            when (canAccountMigration) {
                is AlreadyMoved -> TODO()
                is CanAccountMigration -> {
                    fromActor.moveTo = toActorId
                    actorRepository.save(fromActor)
                }

                is CircularReferences -> TODO()
                is SelfReferences -> TODO()
                is AlsoKnownAsNotFound -> TODO()
            }
        }
    }
}
