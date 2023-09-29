package dev.usbharu.hideout.service.post

import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.query.mastodon.StatusQueryService
import dev.usbharu.hideout.repository.MongoTimelineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Pageable

class MongoGenerateTimelineService(
    private val mongoTimelineRepository: MongoTimelineRepository,
    private val statusQueryService: StatusQueryService
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
        val timelines =
            withContext(Dispatchers.IO) {
                mongoTimelineRepository.findByUserIdAndTimelineIdAndPostIdBetweenAndLocal(
                    forUserId, 0, maxId, minId, localOnly, Pageable.ofSize(limit)
                )
            }
        return statusQueryService.findByPostIds(timelines.flatMap { setOfNotNull(it.postId, it.replyId, it.repostId) })
    }
}
