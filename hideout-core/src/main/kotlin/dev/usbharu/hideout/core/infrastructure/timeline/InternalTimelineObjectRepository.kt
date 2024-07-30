package dev.usbharu.hideout.core.infrastructure.timeline

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.support.page.PaginationList
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject

interface InternalTimelineObjectRepository {
    suspend fun save(timelineObject: TimelineObject): TimelineObject

    suspend fun saveAll(timelineObjectList: List<TimelineObject>): List<TimelineObject>

    suspend fun findByPostId(postId: PostId): List<TimelineObject>

    suspend fun deleteByPostId(postId: PostId)

    suspend fun deleteByTimelineIdAndActorId(timelineId: TimelineId, actorId: ActorId)

    suspend fun deleteByTimelineId(timelineId: TimelineId)
    suspend fun findByTimelineId(
        timelineId: TimelineId,
        internalTimelineObjectOption: InternalTimelineObjectOption? = null,
        page: Page? = null
    ): PaginationList<TimelineObject, PostId>
}

data class InternalTimelineObjectOption(
    val localOnly: Boolean? = null,
    val remoteOnly: Boolean? = null,
    val mediaOnly: Boolean? = null
)