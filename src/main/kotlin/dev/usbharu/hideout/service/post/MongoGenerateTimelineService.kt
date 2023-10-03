package dev.usbharu.hideout.service.post

import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.domain.model.hideout.entity.Timeline
import dev.usbharu.hideout.query.mastodon.StatusQueryService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("hideout.use-mongodb", havingValue = "", matchIfMissing = false)
class MongoGenerateTimelineService(
    private val statusQueryService: StatusQueryService,
    private val mongoTemplate: MongoTemplate
) :
    GenerateTimelineService {
    override suspend fun getTimeline(
        forUserId: Long?,
        localOnly: Boolean,
        mediaOnly: Boolean,
        maxId: Long?,
        minId: Long?,
        sinceId: Long?,
        limit: Int
    ): List<Status> {
        val query = Query()
        if (forUserId != null) {
            val criteria = Criteria.where("userId").`is`(forUserId)
            query.addCriteria(criteria)
        }
        if (localOnly) {
            val criteria = Criteria.where("isLocal").`is`(true)
            query.addCriteria(criteria)
        }
        if (maxId != null) {
            val criteria = Criteria.where("postId").lt(maxId)
            query.addCriteria(criteria)
        }
        if (minId != null) {
            val criteria = Criteria.where("postId").gt(minId)
            query.addCriteria(criteria)
        }

        val timelines = mongoTemplate.find(query.limit(limit), Timeline::class.java)

        return statusQueryService.findByPostIds(timelines.flatMap { setOfNotNull(it.postId, it.replyId, it.repostId) })
    }
}
