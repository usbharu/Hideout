package dev.usbharu.hideout.core.infrastructure.mongorepository

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.EmojiId
import dev.usbharu.hideout.core.domain.model.filter.FilterId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectWarnFilter
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.infrastructure.timeline.InternalTimelineObjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class MongoInternalTimelineObjectRepository(private val springDataMongoTimelineObjectRepository: SpringDataMongoTimelineObjectRepository) :
    InternalTimelineObjectRepository {
    override suspend fun save(timelineObject: TimelineObject): TimelineObject {
        springDataMongoTimelineObjectRepository.save(SpringDataMongoTimelineObject.of(timelineObject))
        return timelineObject
    }

    override suspend fun saveAll(timelineObjectList: List<TimelineObject>): List<TimelineObject> {
        springDataMongoTimelineObjectRepository.saveAll(timelineObjectList.map { SpringDataMongoTimelineObject.of(it) })
            .collect()
        return timelineObjectList
    }

    override suspend fun findByPostId(postId: PostId): List<TimelineObject> {
        return springDataMongoTimelineObjectRepository.findByPostId(postId.id).map { it.toTimelineObject() }.toList()
    }

    override suspend fun deleteByPostId(postId: PostId) {
        springDataMongoTimelineObjectRepository.deleteByPostId(postId.id)
    }

    override suspend fun deleteByTimelineIdAndActorId(timelineId: TimelineId, actorId: ActorId) {
        springDataMongoTimelineObjectRepository.deleteByTimelineIdAndPostActorId(timelineId.value, actorId.id)
    }

    override suspend fun deleteByTimelineId(timelineId: TimelineId) {
        springDataMongoTimelineObjectRepository.deleteByTimelineId(timelineId.value)
    }


}

@Document
data class SpringDataMongoTimelineObject(
    val id: Long,
    val userDetailId: Long,
    val timelineId: Long,
    val postId: Long,
    val postActorId: Long,
    val postCreatedAt: Long,
    val replyId: Long?,
    val repostId: Long?,
    val visibility: Visibility,
    val isPureRepost: Boolean,
    val mediaIds: List<Long>,
    val emojiIds: List<Long>,
    val visibleActors: List<Long>,
    val hasMediaInRepost: Boolean,
    val lastUpdatedAt: Long,
    val warnFilters: List<SpringDataMongoTimelineObjectWarnFilter>
) {

    fun toTimelineObject(): TimelineObject {
        return TimelineObject(
            TimelineObjectId(id),
            UserDetailId(userDetailId),
            TimelineId(timelineId),
            PostId(postId),
            ActorId(postActorId),
            Instant.ofEpochSecond(postCreatedAt),
            replyId?.let { PostId(it) },
            repostId?.let { PostId(it) },
            visibility,
            isPureRepost,
            mediaIds.map { MediaId(it) },
            emojiIds.map { EmojiId(it) },
            visibleActors.map { ActorId(it) },
            hasMediaInRepost,
            Instant.ofEpochSecond(lastUpdatedAt),
            warnFilters.map { it.toTimelineObjectWarnFilter() }
        )
    }

    companion object {
        fun of(timelineObject: TimelineObject): SpringDataMongoTimelineObject {
            return SpringDataMongoTimelineObject(
                timelineObject.id.value,
                timelineObject.userDetailId.id,
                timelineObject.timelineId.value,
                timelineObject.postId.id,
                timelineObject.postActorId.id,
                timelineObject.postCreatedAt.epochSecond,
                timelineObject.replyId?.id,
                timelineObject.repostId?.id,
                timelineObject.visibility,
                timelineObject.isPureRepost,
                timelineObject.mediaIds.map { it.id },
                timelineObject.emojiIds.map { it.emojiId },
                timelineObject.visibleActors.map { it.id },
                timelineObject.hasMediaInRepost,
                timelineObject.lastUpdatedAt.epochSecond,
                timelineObject.warnFilters.map { SpringDataMongoTimelineObjectWarnFilter.of(it) }
            )
        }
    }
}

data class SpringDataMongoTimelineObjectWarnFilter(
    val filterId: Long,
    val matchedKeyword: String
) {

    fun toTimelineObjectWarnFilter(): TimelineObjectWarnFilter {
        return TimelineObjectWarnFilter(
            FilterId(filterId),
            matchedKeyword
        )
    }

    companion object {
        fun of(timelineObjectWarnFilter: TimelineObjectWarnFilter): SpringDataMongoTimelineObjectWarnFilter {
            return SpringDataMongoTimelineObjectWarnFilter(
                timelineObjectWarnFilter.filterId.id,
                timelineObjectWarnFilter.matchedKeyword
            )
        }
    }
}

interface SpringDataMongoTimelineObjectRepository : CoroutineCrudRepository<SpringDataMongoTimelineObject, Long> {
    fun findByPostId(postId: Long): Flow<SpringDataMongoTimelineObject>

    suspend fun deleteByPostId(postId: Long)

    suspend fun deleteByTimelineIdAndPostActorId(timelineId: Long, postActorId: Long)

    suspend fun deleteByTimelineId(timelineId: Long)
}