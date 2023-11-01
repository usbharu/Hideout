package dev.usbharu.hideout.core.infrastructure.mongorepository

import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

@Suppress("LongParameterList", "FunctionMaxLength")
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
