package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Timeline
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

interface MongoTimelineRepository : MongoRepository<Timeline, Long> {
    fun findByUserId(id: Long): List<Timeline>
    fun findByUserIdAndTimelineId(userId: Long, timelineId: Long): List<Timeline>
    fun findByUserIdAndTimelineIdAndPostIdBetweenAndIsLocal(
        userId: Long?,
        timelineId: Long?,
        postIdMin: Long?,
        postIdMax: Long?,
        isLocal: Boolean?,
        pageable: Pageable
    ): List<Timeline>
}
