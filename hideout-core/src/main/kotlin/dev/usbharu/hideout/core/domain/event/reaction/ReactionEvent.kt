package dev.usbharu.hideout.core.domain.event.reaction

import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

class ReactionEventFactory(private val reaction: Reaction) {
    fun createEvent(reactionEvent: ReactionEvent): DomainEvent<ReactionEventBody> =
        DomainEvent.create(reactionEvent.eventName, ReactionEventBody(reaction))
}

class ReactionEventBody(
    reaction: Reaction
) : DomainEventBody(mapOf("reactionId" to reaction.id)) {
    fun getReactionId(): ReactionId = toMap()["reactionId"] as ReactionId
}

enum class ReactionEvent(val eventName: String) {
    CREATE("ReactionCreate"),
    DELETE("ReactionDelete"),
}