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
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.shared.Domain
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable
import java.net.URI
import java.time.Instant

@Suppress("LongParameterList", "ClassOrdering")
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
    roles: Set<Role>,
    icon: MediaId?,
    banner: MediaId?,
) : DomainEventStorable() {

    var banner = banner
        private set

    fun setBannerUrl(banner: MediaId?, actor: Actor) {
        addDomainEvent(ActorDomainEventFactory(this).createEvent(UPDATE))
        this.banner = banner
    }

    var icon = icon
        private set

    fun setIconUrl(icon: MediaId?, actor: Actor) {
        addDomainEvent(ActorDomainEventFactory(this).createEvent(UPDATE))
        this.icon = icon
    }

    var roles = roles
        private set

    fun setRole(roles: Set<Role>, actor: Actor) {
        require(actor.roles.contains(Role.ADMINISTRATOR).not())

        this.roles = roles
    }

    var suspend = suspend
        set(value) {
            if (field != value && value) {
                addDomainEvent(ActorDomainEventFactory(this).createEvent(ACTOR_SUSPEND))
            } else if (field != value && !value) {
                addDomainEvent(ActorDomainEventFactory(this).createEvent(ACTOR_UNSUSPEND))
            }
            field = value
        }

    var alsoKnownAs = alsoKnownAs
        set(value) {
            require(value.none { it == id })
            field = value
        }

    var moveTo = moveTo
        set(value) {
            require(value != id)
            addDomainEvent(ActorDomainEventFactory(this).createEvent(MOVE))
            field = value
        }

    var emojis = emojiIds
        private set

    var description = description
        set(value) {
            addDomainEvent(ActorDomainEventFactory(this).createEvent(UPDATE))
            field = value
        }
    var screenName = screenName
        set(value) {
            addDomainEvent(ActorDomainEventFactory(this).createEvent(UPDATE))
            field = value
        }

    var deleted = deleted
        private set

    fun delete() {
        if (deleted.not()) {
            addDomainEvent(ActorDomainEventFactory(this).createEvent(DELETE))
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
        addDomainEvent(ActorDomainEventFactory(this).createEvent(CHECK_UPDATE))
    }
}
