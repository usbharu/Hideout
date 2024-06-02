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

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.instance.InstanceRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.service.actor.local.LocalActorDomainService
import dev.usbharu.hideout.core.domain.service.userdetail.UserDetailDomainService
import dev.usbharu.hideout.core.infrastructure.factory.ActorFactoryImpl
import org.springframework.stereotype.Service

@Service
class RegisterLocalActorApplicationService(
    private val transaction: Transaction,
    private val actorDomainService: LocalActorDomainService,
    private val actorRepository: ActorRepository,
    private val actorFactoryImpl: ActorFactoryImpl,
    private val instanceRepository: InstanceRepository,
    private val applicationConfig: ApplicationConfig,
    private val userDetailDomainService: UserDetailDomainService,
    private val userDetailRepository: UserDetailRepository,
    private val idGenerateService: IdGenerateService,
) {
    suspend fun register(registerLocalActor: RegisterLocalActor) {
        transaction.transaction {
            if (actorDomainService.usernameAlreadyUse(registerLocalActor.name)) {
                // todo 適切な例外を考える
                throw Exception("Username already exists")
            }
            val instance = instanceRepository.findByUrl(applicationConfig.url.toURI())!!

            val actor = actorFactoryImpl.createLocal(
                registerLocalActor.name,
                actorDomainService.generateKeyPair(),
                instance.id
            )
            actorRepository.save(actor)
            val userDetail = UserDetail.create(
                id = UserDetailId(idGenerateService.generateId()),
                actorId = actor.id,
                password = userDetailDomainService.hashPassword(registerLocalActor.password),
            )
            userDetailRepository.save(userDetail)
        }
    }
}
