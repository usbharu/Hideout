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

package dev.usbharu.hideout.core.domain.model.actor

import dev.usbharu.hideout.core.domain.event.actor.ActorDomainEventFactory
import dev.usbharu.hideout.core.domain.event.actor.ActorEvent.*
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.shared.Domain
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable
import java.net.URI
import java.time.Instant

class Actor2 private constructor(
    val id: ActorId,
    val name: ActorName,
    val domain: Domain,
    screenName: ActorScreenName,
    description: ActorDescription,
    val inbox: URI,
    val outbox: URI,
    val url: URI,
    val publicKey: ActorPublicKey,
    val privateKey: ActorPrivateKey? = null,
    val createdAt: Instant,
    val keyId: ActorKeyId,
    val followersEndpoint: URI?,
    val followingEndpoint: URI?,
    val instance: InstanceId,
    var locked: Boolean,
    var followersCount: ActorRelationshipCount?,
    var followingCount: ActorRelationshipCount?,
    var postsCount: ActorPostsCount,
    var lastPostDate: Instant? = null,
    suspend: Boolean,
    var lastUpdate: Instant = createdAt,
    alsoKnownAs: Set<ActorId> = emptySet(),
    moveTo: ActorId? = null,
) : DomainEventStorable() {

    var suspend = suspend
        set(value) {
            if (field != value && value) {
                addDomainEvent(ActorDomainEventFactory(this).createEvent(actorSuspend))
            } else if (field != value && !value) {
                addDomainEvent(ActorDomainEventFactory(this).createEvent(actorUnsuspend))
            }
            field = value
        }

    var alsoKnownAs = alsoKnownAs
        set(value) {
            require(value.find { it == id } == null)
            field = value
        }

    var moveTo = moveTo
        set(value) {
            require(moveTo != id)
            addDomainEvent(ActorDomainEventFactory(this).createEvent(move))
            field = value
        }


    val emojis
        get() = screenName.emojis + description.emojis

    var description = description
        set(value) {
            addDomainEvent(ActorDomainEventFactory(this).createEvent(update))
            field = value
        }
    var screenName = screenName
        set(value) {
            addDomainEvent(ActorDomainEventFactory(this).createEvent(update))
            field = value
        }


    fun delete() {
        addDomainEvent(ActorDomainEventFactory(this).createEvent(delete))
    }

    fun checkUpdate() {
        addDomainEvent(ActorDomainEventFactory(this).createEvent(checkUpdate))
    }

    abstract class Actor2Factory {
        protected suspend fun internalCreate(
            id: ActorId,
            name: ActorName,
            domain: Domain,
            screenName: ActorScreenName,
            description: ActorDescription,
            inbox: URI,
            outbox: URI,
            url: URI,
            publicKey: ActorPublicKey,
            privateKey: ActorPrivateKey? = null,
            createdAt: Instant,
            keyId: ActorKeyId,
            followersEndpoint: URI,
            followingEndpoint: URI,
            instance: InstanceId,
            locked: Boolean,
            followersCount: ActorRelationshipCount,
            followingCount: ActorRelationshipCount,
            postsCount: ActorPostsCount,
            lastPostDate: Instant? = null,
            suspend: Boolean,
        ): Actor2 {
            return Actor2(
                id = id,
                name = name,
                domain = domain,
                screenName = screenName,
                description = description,
                inbox = inbox,
                outbox = outbox,
                url = url,
                publicKey = publicKey,
                privateKey = privateKey,
                createdAt = createdAt,
                keyId = keyId,
                followersEndpoint = followersEndpoint,
                followingEndpoint = followingEndpoint,
                instance = instance,
                locked = locked,
                followersCount = followersCount,
                followingCount = followingCount,
                postsCount = postsCount,
                lastPostDate = lastPostDate,
                suspend = suspend
            )
        }
    }
}
