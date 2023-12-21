package dev.usbharu.hideout.core.infrastructure.mongorepository

import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.exception.resource.DuplicateException
import dev.usbharu.hideout.core.domain.exception.resource.ResourceAccessException
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnProperty("hideout.use-mongodb", havingValue = "true", matchIfMissing = false)
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

    override suspend fun saveAll(timelines: List<Timeline>): List<Timeline> {
        try {
            return mongoTimelineRepository.saveAll(timelines)
        } catch (e: DuplicateKeyException) {
            throw DuplicateException("Timeline duplicate.", e)
        } catch (e: DataAccessException) {
            throw ResourceAccessException(e)
        }
    }

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
