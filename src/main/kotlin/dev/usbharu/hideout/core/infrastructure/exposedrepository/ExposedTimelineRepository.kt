package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository

@Repository
@Qualifier("jdbc")
@ConditionalOnProperty("hideout.use-mongodb", havingValue = "false", matchIfMissing = true)
class ExposedTimelineRepository(private val idGenerateService: IdGenerateService) : TimelineRepository {
    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(timeline: Timeline): Timeline {
        if (Timelines.select { Timelines.id eq timeline.id }.singleOrNull() == null) {
            Timelines.insert {
                it[id] = timeline.id
                it[userId] = timeline.userId
                it[timelineId] = timeline.timelineId
                it[postId] = timeline.postId
                it[postUserId] = timeline.postActorId
                it[createdAt] = timeline.createdAt
                it[replyId] = timeline.replyId
                it[repostId] = timeline.repostId
                it[visibility] = timeline.visibility.ordinal
                it[sensitive] = timeline.sensitive
                it[isLocal] = timeline.isLocal
                it[isPureRepost] = timeline.isPureRepost
                it[mediaIds] = timeline.mediaIds.joinToString(",")
            }
        } else {
            Timelines.update({ Timelines.id eq timeline.id }) {
                it[userId] = timeline.userId
                it[timelineId] = timeline.timelineId
                it[postId] = timeline.postId
                it[postUserId] = timeline.postActorId
                it[createdAt] = timeline.createdAt
                it[replyId] = timeline.replyId
                it[repostId] = timeline.repostId
                it[visibility] = timeline.visibility.ordinal
                it[sensitive] = timeline.sensitive
                it[isLocal] = timeline.isLocal
                it[isPureRepost] = timeline.isPureRepost
                it[mediaIds] = timeline.mediaIds.joinToString(",")
            }
        }
        return timeline
    }

    override suspend fun saveAll(timelines: List<Timeline>): List<Timeline> {
        Timelines.batchInsert(timelines, true, false) {
            this[Timelines.id] = it.id
            this[Timelines.userId] = it.userId
            this[Timelines.timelineId] = it.timelineId
            this[Timelines.postId] = it.postId
            this[Timelines.postUserId] = it.postActorId
            this[Timelines.createdAt] = it.createdAt
            this[Timelines.replyId] = it.replyId
            this[Timelines.repostId] = it.repostId
            this[Timelines.visibility] = it.visibility.ordinal
            this[Timelines.sensitive] = it.sensitive
            this[Timelines.isLocal] = it.isLocal
            this[Timelines.isPureRepost] = it.isPureRepost
            this[Timelines.mediaIds] = it.mediaIds.joinToString(",")
        }
        return timelines
    }

    override suspend fun findByUserId(id: Long): List<Timeline> =
        Timelines.select { Timelines.userId eq id }.map { it.toTimeline() }

    override suspend fun findByUserIdAndTimelineId(userId: Long, timelineId: Long): List<Timeline> =
        Timelines.select { Timelines.userId eq userId and (Timelines.timelineId eq timelineId) }
            .map { it.toTimeline() }
}

fun ResultRow.toTimeline(): Timeline {
    return Timeline(
        id = this[Timelines.id],
        userId = this[Timelines.userId],
        timelineId = this[Timelines.timelineId],
        postId = this[Timelines.postId],
        postActorId = this[Timelines.postUserId],
        createdAt = this[Timelines.createdAt],
        replyId = this[Timelines.replyId],
        repostId = this[Timelines.repostId],
        visibility = Visibility.values().first { it.ordinal == this[Timelines.visibility] },
        sensitive = this[Timelines.sensitive],
        isLocal = this[Timelines.isLocal],
        isPureRepost = this[Timelines.isPureRepost],
        mediaIds = this[Timelines.mediaIds].split(",").mapNotNull { it.toLongOrNull() }
    )
}

object Timelines : Table("timelines") {
    val id = long("id")
    val userId = long("user_id")
    val timelineId = long("timeline_id")
    val postId = long("post_id")
    val postUserId = long("post_user_id")
    val createdAt = long("created_at")
    val replyId = long("reply_id").nullable()
    val repostId = long("repost_id").nullable()
    val visibility = integer("visibility")
    val sensitive = bool("sensitive")
    val isLocal = bool("is_local")
    val isPureRepost = bool("is_pure_repost")
    val mediaIds = varchar("media_ids", 255)

    override val primaryKey: PrimaryKey = PrimaryKey(id)

    init {
        uniqueIndex(userId, timelineId, postId)
    }
}
