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

package dev.usbharu.hideout.core.domain.model.post

import dev.usbharu.hideout.core.domain.event.post.PostDomainEventFactory
import dev.usbharu.hideout.core.domain.event.post.PostEvent
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.shared.domainevent.DomainEventStorable
import java.net.URI
import java.time.Instant

class Post2 private constructor(
    val id: PostId,
    actorId: ActorId,
    overview: PostOverview? = null,
    content: PostContent,
    val createdAt: Instant,
    visibility: Visibility,
    val url: URI,
    val repostId: PostId?,
    val replyId: PostId?,
    sensitive: Boolean,
    val apId: URI,
    deleted: Boolean,
    mediaIds: List<MediaId>,
    visibleActors: List<ActorId> = emptyList(),
    hide: Boolean = false,
    moveTo: PostId? = null,
) : DomainEventStorable() {

    var actorId = actorId
        private set
        get() {
            if (deleted) {
                return ActorId.ghost
            }
            return field
        }

    var visibility = visibility
        set(value) {
            require(value != Visibility.DIRECT)
            require(field.ordinal >= value.ordinal)

            require(deleted.not())

            if (field != value) {
                addDomainEvent(PostDomainEventFactory(this).createEvent(PostEvent.update))
            }
            field = value
        }

    var visibleActors = visibleActors
        set(value) {
            require(deleted.not())
            if (visibility == Visibility.DIRECT) {
                addDomainEvent(PostDomainEventFactory(this).createEvent(PostEvent.update))
                field = field.plus(value).distinct()
            }
        }

    var content = content
        get() {
            if (hide) {
                return PostContent.empty
            }
            return field
        }
        set(value) {
            require(deleted.not())
            if (field != value) {
                addDomainEvent(PostDomainEventFactory(this).createEvent(PostEvent.update))
            }
            field = value
        }

    var overview = overview
        get() {
            if (hide) {
                return null
            }
            return field
        }
        set(value) {
            require(deleted.not())
            if (field != value) {
                addDomainEvent(PostDomainEventFactory(this).createEvent(PostEvent.update))
            }
            field = value
        }

    var sensitive = sensitive
        set(value) {
            require(deleted.not())
            if (field != value) {
                addDomainEvent(PostDomainEventFactory(this).createEvent(PostEvent.update))
            }
            field = value
        }

    val text
        get() = content.text

    val emojiIds
        get() = content.emojiIds

    var mediaIds = mediaIds
        get() {
            if (hide) {
                return emptyList()
            }
            return field
        }
        private set

    fun addMediaIds(mediaIds: List<MediaId>) {
        require(deleted.not())
        addDomainEvent(PostDomainEventFactory(this).createEvent(PostEvent.update))
        this.mediaIds = this.mediaIds.plus(mediaIds).distinct()
    }

    var deleted = deleted
        private set

    fun delete() {
        if (deleted.not()) {
            addDomainEvent(PostDomainEventFactory(this).createEvent(PostEvent.delete))
            content = PostContent.empty
            overview = null
            mediaIds = emptyList()

        }
        deleted = true
    }

    fun checkUpdate() {
        addDomainEvent(PostDomainEventFactory(this).createEvent(PostEvent.checkUpdate))
    }

    fun restore(content: PostContent, overview: PostOverview?, mediaIds: List<MediaId>) {
        deleted = false
        this.content = content
        this.overview = overview
        this.mediaIds = mediaIds
    }

    var hide = hide
        private set

    fun hide() {
        hide = true
    }

    fun show() {
        hide = false
    }

    var moveTo = moveTo
        private set

    fun moveTo(moveTo: PostId) {
        require(this.moveTo == null)
        this.moveTo = moveTo
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Post2

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    abstract class PostFactory {
        protected fun create(
            id: PostId,
            actorId: ActorId,
            overview: PostOverview? = null,
            content: PostContent,
            createdAt: Instant,
            visibility: Visibility,
            url: URI,
            repostId: PostId?,
            replyId: PostId?,
            sensitive: Boolean,
            apId: URI,
            deleted: Boolean,
            mediaIds: List<MediaId>,
            hide: Boolean,
        ): Post2 {
            return Post2(
                id = id,
                actorId = actorId,
                overview = overview,
                content = content,
                createdAt = createdAt,
                visibility = visibility,
                url = url,
                repostId = repostId,
                replyId = replyId,
                sensitive = sensitive,
                apId = apId,
                deleted = deleted,
                mediaIds = mediaIds,
                hide = hide
            ).apply { addDomainEvent(PostDomainEventFactory(this).createEvent(PostEvent.create)) }
        }
    }
}