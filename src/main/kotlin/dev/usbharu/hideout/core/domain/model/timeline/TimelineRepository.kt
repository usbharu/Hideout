package dev.usbharu.hideout.core.domain.model.timeline

interface TimelineRepository {
    suspend fun generateId(): Long
    suspend fun save(timeline: Timeline): Timeline
    suspend fun saveAll(timelines: List<Timeline>): List<Timeline>
    suspend fun findByUserId(id: Long): List<Timeline>
    suspend fun findByUserIdAndTimelineId(userId: Long, timelineId: Long): List<Timeline>
}
