package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timelinerelationship.Visible

data class AddTimelineRelationship(
    val timelineId: TimelineId,
    val actorId: ActorId,
    val visible: Visible
)
