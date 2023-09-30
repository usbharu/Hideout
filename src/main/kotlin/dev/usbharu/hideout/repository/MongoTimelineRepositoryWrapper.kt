package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Timeline
import dev.usbharu.hideout.service.core.IdGenerateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository

@Repository
@Suppress("InjectDispatcher")
class MongoTimelineRepositoryWrapper(
    private val mongoTimelineRepository: MongoTimelineRepository,
    private val idGenerateService: IdGenerateService
) :
    TimelineRepository {
    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(timeline: Timeline): Timeline {
        return withContext(Dispatchers.IO) {
            mongoTimelineRepository.save(timeline)
        }
    }

    override suspend fun saveAll(timelines: List<Timeline>): List<Timeline> =
        mongoTimelineRepository.saveAll(timelines)

    override suspend fun findByUserId(id: Long): List<Timeline> {
        return withContext(Dispatchers.IO) {
            mongoTimelineRepository.findByUserId(id)
        }
    }

    override suspend fun findByUserIdAndTimelineId(userId: Long, timelineId: Long): List<Timeline> {
        return withContext(Dispatchers.IO) {
            mongoTimelineRepository.findByUserIdAndTimelineId(userId, timelineId)
        }
    }
}
