package dev.usbharu.hideout.core.domain.model.timelinerelationship

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId

class TimelineRelationship(
    val timelineRelationshipId: TimelineRelationshipId,
    val actorId: ActorId?,
    val instanceId: InstanceId?
)