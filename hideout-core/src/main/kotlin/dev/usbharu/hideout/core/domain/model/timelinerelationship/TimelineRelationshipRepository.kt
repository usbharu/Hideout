package dev.usbharu.hideout.core.domain.model.timelinerelationship

import dev.usbharu.hideout.core.domain.model.actor.ActorId

interface TimelineRelationshipRepository {
    suspend fun save(timelineRelationship: TimelineRelationship): TimelineRelationship
    suspend fun delete(timelineRelationship: TimelineRelationship)

    suspend fun findByActorId(actorId: ActorId): List<TimelineRelationship>
}