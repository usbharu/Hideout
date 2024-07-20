package dev.usbharu.hideout.core.domain.model.timelinerelationship

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId

class TimelineRelationship(
    val id: TimelineRelationshipId,
    val timelineId: TimelineId,
    val actorId: ActorId,
    val visible: Visible
)

enum class Visible {
    PUBLIC,
    UNLISTED,
    FOLLOWERS,
    DIRECT
}