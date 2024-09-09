package dev.usbharu.hideout.core.domain.model.reaction

import dev.usbharu.hideout.core.domain.event.reaction.ReactionEvent
import dev.usbharu.hideout.core.domain.event.reaction.ReactionEventFactory
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable
import java.time.Instant

class Reaction(
    val id: ReactionId,
    val postId: PostId,
    val actorId: ActorId,
    val customEmojiId: CustomEmojiId?,
    val unicodeEmoji: UnicodeEmoji,
    val createdAt: Instant
) : DomainEventStorable() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Reaction

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun delete() {
        addDomainEvent(ReactionEventFactory(this).createEvent(ReactionEvent.DELETE))
    }

    companion object {
        fun create(
            id: ReactionId,
            postId: PostId,
            actorId: ActorId,
            customEmojiId: CustomEmojiId?,
            unicodeEmoji: UnicodeEmoji,
            createdAt: Instant
        ): Reaction {
            return Reaction(
                id,
                postId,
                actorId,
                customEmojiId,
                unicodeEmoji,
                createdAt
            ).apply { addDomainEvent(ReactionEventFactory(this).createEvent(ReactionEvent.CREATE)) }
        }
    }
}
