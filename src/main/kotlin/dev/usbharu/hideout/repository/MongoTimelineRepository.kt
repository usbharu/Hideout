package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Timeline
import org.springframework.data.mongodb.repository.MongoRepository

interface MongoTimelineRepository : TimelineRepository, MongoRepository<Timeline, Long> {
    override fun findByUserId(id: Long): List<Timeline>
    override fun findByUserIdAndTimelineId(userId: Long, timelineId: Long): List<Timeline>
}
