package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Timeline
import org.springframework.data.mongodb.repository.MongoRepository

interface MongoTimelineRepository : MongoRepository<Timeline, Long> {


    fun findByUserId(id: Long): List<Timeline>
    fun findByUserIdAndTimelineId(userId: Long, timelineId: Long): List<Timeline>
}
