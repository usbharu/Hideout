package dev.usbharu.hideout.core.service.timeline

import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.application.infrastructure.exposed.pagination
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Timelines
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.interfaces.api.status.StatusQuery
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("hideout.use-mongodb", havingValue = "false", matchIfMissing = true)
class ExposedGenerateTimelineService(private val statusQueryService: StatusQueryService) : GenerateTimelineService {
    override suspend fun getTimeline(
        forUserId: Long?,
        localOnly: Boolean,
        mediaOnly: Boolean,
        maxId: Long?,
        minId: Long?,
        sinceId: Long?,
        limit: Int
    ): List<Status> {
        val query = Timelines.selectAll()

        if (forUserId != null) {
            query.andWhere { Timelines.userId eq forUserId }
        }
        if (localOnly) {
            query.andWhere { Timelines.isLocal eq true }
        }
        if (maxId != null) {
            query.andWhere { Timelines.id lessEq maxId }
        }
        if (minId != null) {
            query.andWhere { Timelines.id greaterEq minId }
        }
        val result = query
            .limit(limit)
            .orderBy(Timelines.createdAt, SortOrder.DESC)

        val statusQueries = result.map {
            StatusQuery(
                it[Timelines.postId],
                it[Timelines.replyId],
                it[Timelines.repostId],
                it[Timelines.mediaIds].split(",").mapNotNull { s -> s.toLongOrNull() },
                it[Timelines.emojiIds].split(",").mapNotNull { s -> s.toLongOrNull() }
            )
        }

        return statusQueryService.findByPostIdsWithMediaIds(statusQueries)
    }

    override suspend fun getTimeline(
        forUserId: Long?,
        localOnly: Boolean,
        mediaOnly: Boolean,
        page: Page
    ): PaginationList<Status, Long> {
        val query = Timelines.selectAll()

        if (forUserId != null) {
            query.andWhere { Timelines.userId eq forUserId }
        }
        if (localOnly) {
            query.andWhere { Timelines.isLocal eq true }
        }
        query.pagination(page, Timelines.id)
        val result = query
            .orderBy(Timelines.createdAt, SortOrder.DESC)

        val statusQueries = result.map {
            StatusQuery(
                it[Timelines.postId],
                it[Timelines.replyId],
                it[Timelines.repostId],
                it[Timelines.mediaIds].split(",").mapNotNull { s -> s.toLongOrNull() },
                it[Timelines.emojiIds].split(",").mapNotNull { s -> s.toLongOrNull() }
            )
        }

        val findByPostIdsWithMediaIds = statusQueryService.findByPostIdsWithMediaIds(statusQueries)
        return PaginationList(
            findByPostIdsWithMediaIds,
            findByPostIdsWithMediaIds.lastOrNull()?.id?.toLongOrNull(),
            findByPostIdsWithMediaIds.firstOrNull()?.id?.toLongOrNull()
        )
    }
}
