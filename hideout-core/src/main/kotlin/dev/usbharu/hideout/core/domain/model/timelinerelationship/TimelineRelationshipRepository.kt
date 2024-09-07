package dev.usbharu.hideout.core.domain.model.timelinerelationship

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId

interface TimelineRelationshipRepository {
    suspend fun save(timelineRelationship: TimelineRelationship): TimelineRelationship
    suspend fun delete(timelineRelationship: TimelineRelationship)

    suspend fun findByActorId(actorId: ActorId): List<TimelineRelationship>
    suspend fun findById(timelineRelationshipId: TimelineRelationshipId): TimelineRelationship?
    suspend fun findByTimelineIdAndActorId(timelineId: TimelineId, actorId: ActorId): TimelineRelationship?
}
