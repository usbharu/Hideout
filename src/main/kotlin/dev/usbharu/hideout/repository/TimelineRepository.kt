package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Timeline

interface TimelineRepository {
    fun findByUserId(id: Long): List<Timeline>
    fun findByUserIdAndTimelineId(userId: Long, timelineId: Long): List<Timeline>
}
