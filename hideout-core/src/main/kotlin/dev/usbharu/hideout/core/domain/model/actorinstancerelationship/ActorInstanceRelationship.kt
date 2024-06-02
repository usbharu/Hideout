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

package dev.usbharu.hideout.core.domain.model.actorinstancerelationship

import dev.usbharu.hideout.core.domain.event.actorinstancerelationship.ActorInstanceRelationshipDomainEventFactory
import dev.usbharu.hideout.core.domain.event.actorinstancerelationship.ActorInstanceRelationshipEvent.*
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable

data class ActorInstanceRelationship(
    val actorId: ActorId,
    val instanceId: InstanceId,
    private var blocking: Boolean = false,
    private var muting: Boolean = false,
    private var doNotSendPrivate: Boolean = false,
) : DomainEventStorable() {
    fun block(): ActorInstanceRelationship {
        addDomainEvent(ActorInstanceRelationshipDomainEventFactory(this).createEvent(block))
        blocking = true
        return this
    }

    fun unblock(): ActorInstanceRelationship {
        blocking = false
        return this
    }

    fun mute(): ActorInstanceRelationship {
        addDomainEvent(ActorInstanceRelationshipDomainEventFactory(this).createEvent(mute))
        muting = true
        return this
    }

    fun unmute(): ActorInstanceRelationship {
        addDomainEvent(ActorInstanceRelationshipDomainEventFactory(this).createEvent(unmute))
        muting = false
        return this
    }

    fun doNotSendPrivate(): ActorInstanceRelationship {
        doNotSendPrivate = true
        return this
    }

    fun doSendPrivate(): ActorInstanceRelationship {
        doNotSendPrivate = false
        return this
    }

    fun isBlocking() = blocking

    fun isMuting() = muting

    fun isDoNotSendPrivate() = doNotSendPrivate

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActorInstanceRelationship

        if (actorId != other.actorId) return false
        if (instanceId != other.instanceId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = actorId.hashCode()
        result = 31 * result + instanceId.hashCode()
        return result
    }
}
