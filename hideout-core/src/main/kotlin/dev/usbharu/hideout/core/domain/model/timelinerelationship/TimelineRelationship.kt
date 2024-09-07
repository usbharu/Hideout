package dev.usbharu.hideout.core.domain.model.timelinerelationship

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId

class TimelineRelationship(
    val id: TimelineRelationshipId,
    val timelineId: TimelineId,
    val actorId: ActorId,
    val visible: Visible
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimelineRelationship

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

enum class Visible {
    PUBLIC,
    UNLISTED,
    FOLLOWERS,
    DIRECT
}
