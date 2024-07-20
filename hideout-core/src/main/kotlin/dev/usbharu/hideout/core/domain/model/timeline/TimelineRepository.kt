package dev.usbharu.hideout.core.domain.model.timeline

interface TimelineRepository {
    suspend fun save(timeline: Timeline): Timeline
    suspend fun delete(timeline: Timeline)

    suspend fun findByIds(ids: List<TimelineId>): List<Timeline>

    suspend fun findById(id: TimelineId): Timeline?
}
