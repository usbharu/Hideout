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

package dev.usbharu.hideout.core.domain.event.actorinstancerelationship

import dev.usbharu.hideout.core.domain.model.actorinstancerelationship.ActorInstanceRelationship
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

class ActorInstanceRelationshipDomainEventFactory(private val actorInstanceRelationship: ActorInstanceRelationship) {
    fun createEvent(actorInstanceRelationshipEvent: ActorInstanceRelationshipEvent): DomainEvent {
        return DomainEvent.create(
            actorInstanceRelationshipEvent.eventName,
            ActorInstanceRelationshipEventBody(actorInstanceRelationship)
        )
    }
}

class ActorInstanceRelationshipEventBody(actorInstanceRelationship: ActorInstanceRelationship) :
    DomainEventBody(
        mapOf(
            "actorId" to actorInstanceRelationship.actorId,
            "instanceId" to actorInstanceRelationship.instanceId,
            "muting" to actorInstanceRelationship.isMuting(),
            "blocking" to actorInstanceRelationship.isBlocking(),
            "doNotSendPrivate" to actorInstanceRelationship.isDoNotSendPrivate(),
        )
    )

enum class ActorInstanceRelationshipEvent(val eventName: String) {
    block("ActorInstanceBlock"),
    mute("ActorInstanceMute"),
    unmute("ActorInstanceUnmute"),
}