package dev.usbharu.hideout.core.domain.model.timelinerelationship

interface TimelineRelationshipRepository {
    suspend fun save(timelineRelationship: TimelineRelationship): TimelineRelationship
    suspend fun delete(timelineRelationship: TimelineRelationship)
}