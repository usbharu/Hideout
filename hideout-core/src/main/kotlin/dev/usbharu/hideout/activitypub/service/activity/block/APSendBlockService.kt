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

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.owl.producer.api.OwlProducer
import org.springframework.stereotype.Service

interface APSendBlockService {
    suspend fun sendBlock(actor: Actor, target: Actor)
}

@Service
class ApSendBlockServiceImpl(
    private val applicationConfig: ApplicationConfig,
    private val owlProducer: OwlProducer,
) : APSendBlockService {
    override suspend fun sendBlock(actor: Actor, target: Actor) {
//        val blockJobParam = DeliverBlockJobParam(
//            actor.id,
//            Block(
//                actor.url,
//                "${applicationConfig.url}/block/${actor.id}/${target.id}",
//                target.url
//            ),
//            Reject(
//                actor.url,
//                "${applicationConfig.url}/reject/${actor.id}/${target.id}",
//                Follow(
//                    apObject = actor.url,
//                    actor = target.url
//                )
//            ),
//            target.inbox
//        )
//        owlProducer.publishTask(blockJobParam)
    }
}
