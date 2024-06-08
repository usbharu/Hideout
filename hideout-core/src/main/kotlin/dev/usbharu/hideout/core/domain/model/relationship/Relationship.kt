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

package dev.usbharu.hideout.core.domain.model.relationship

import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEvent
import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEventFactory
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable

class Relationship(
    val actorId: ActorId,
    val targetActorId: ActorId,
    following: Boolean,
    blocking: Boolean,
    muting: Boolean,
    followRequesting: Boolean,
    mutingFollowRequest: Boolean,
) : DomainEventStorable() {

    var following: Boolean = following
        private set
    var blocking: Boolean = blocking
        private set

    var muting: Boolean = muting
        private set
    var followRequesting: Boolean = followRequesting
        private set
    var mutingFollowRequest: Boolean = mutingFollowRequest
        private set

    fun follow() {
        require(blocking.not())
        following = true
        addDomainEvent(RelationshipEventFactory(this).createEvent(RelationshipEvent.follow))
    }

    fun unfollow() {
        following = false
        addDomainEvent(RelationshipEventFactory(this).createEvent(RelationshipEvent.unfollow))
    }

    fun block() {
        require(following.not())
        blocking = true
        addDomainEvent(RelationshipEventFactory(this).createEvent(RelationshipEvent.block))
    }

    fun unblock() {
        blocking = false
        addDomainEvent(RelationshipEventFactory(this).createEvent(RelationshipEvent.unblock))
    }

    fun mute() {
        muting = true
        addDomainEvent(RelationshipEventFactory(this).createEvent(RelationshipEvent.mute))
    }

    fun unmute() {
        muting = false
        addDomainEvent(RelationshipEventFactory(this).createEvent(RelationshipEvent.unmute))
    }

    fun muteFollowRequest() {
        mutingFollowRequest = true
    }

    fun unmuteFollowRequest() {
        mutingFollowRequest = false
    }

    fun followRequest() {
        require(blocking.not())
        followRequesting = true
        addDomainEvent(RelationshipEventFactory(this).createEvent(RelationshipEvent.followRequest))
    }

    fun unfollowRequest() {
        followRequesting = false
        addDomainEvent(RelationshipEventFactory(this).createEvent(RelationshipEvent.unfollowRequest))
    }

    fun acceptFollowRequest() {
        follow()
        followRequesting = false
    }

    fun rejectFollowRequest() {
        followRequesting = false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Relationship

        if (actorId != other.actorId) return false
        if (targetActorId != other.targetActorId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = actorId.hashCode()
        result = 31 * result + targetActorId.hashCode()
        return result
    }


    companion object {
        fun default(actorId: ActorId, targetActorId: ActorId): Relationship = Relationship(
            actorId = actorId,
            targetActorId = targetActorId,
            following = false,
            blocking = false,
            muting = false,
            followRequesting = false,
            mutingFollowRequest = false
        )
    }
}
