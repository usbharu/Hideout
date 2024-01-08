package dev.usbharu.hideout.core.service.timeline

import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.interfaces.api.status.StatusQuery
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("hideout.use-mongodb", havingValue = "true", matchIfMissing = false)
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

        query.limit(limit)
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"))

        val timelines = mongoTemplate.find(query, Timeline::class.java)

        return statusQueryService.findByPostIdsWithMediaIds(
            timelines.map {
                StatusQuery(
                    it.postId,
                    it.replyId,
                    it.repostId,
                    it.mediaIds,
                    it.emojiIds
                )
            }
        )
    }
}
