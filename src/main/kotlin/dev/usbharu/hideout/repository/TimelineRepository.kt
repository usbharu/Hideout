package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Timeline

interface TimelineRepository {
    suspend fun save(timeline: Timeline): Timeline
    suspend fun saveAll(timelines: List<Timeline>): List<Timeline>
    suspend fun findByUserId(id: Long): List<Timeline>
    suspend fun findByUserIdAndTimelineId(userId: Long, timelineId: Long): List<Timeline>
}
