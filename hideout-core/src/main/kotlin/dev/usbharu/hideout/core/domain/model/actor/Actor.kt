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
import dev.usbharu.hideout.core.domain.model.emoji.EmojiId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.shared.Domain
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable
import java.net.URI
import java.time.Instant

class Actor(
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
    var lastPostAt: Instant? = null,
    suspend: Boolean,
    var lastUpdateAt: Instant = createdAt,
    alsoKnownAs: Set<ActorId> = emptySet(),
    moveTo: ActorId? = null,
    emojiIds: Set<EmojiId>,
    deleted: Boolean,
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

    var emojis = emojiIds
        private set

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

    var deleted = deleted
        private set

    fun delete() {
        if (deleted.not()) {
            addDomainEvent(ActorDomainEventFactory(this).createEvent(delete))
            screenName = ActorScreenName.empty
            description = ActorDescription.empty
            emojis = emptySet()
            lastPostAt = null
            postsCount = ActorPostsCount.ZERO
            followersCount = null
            followingCount = null
        }
    }

    fun restore() {
        deleted = false
        checkUpdate()
    }

    fun checkUpdate() {
        addDomainEvent(ActorDomainEventFactory(this).createEvent(checkUpdate))
    }
}
