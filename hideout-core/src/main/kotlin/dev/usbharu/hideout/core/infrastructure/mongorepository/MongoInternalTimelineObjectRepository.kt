package dev.usbharu.hideout.core.infrastructure.mongorepository

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject
import dev.usbharu.hideout.core.infrastructure.timeline.InternalTimelineObjectRepository
import org.springframework.stereotype.Repository

@Repository
class MongoInternalTimelineObjectRepository : InternalTimelineObjectRepository {
    override suspend fun save(timelineObject: TimelineObject): TimelineObject {
        TODO("Not yet implemented")
    }

    override suspend fun saveAll(timelineObjectList: List<TimelineObject>): List<TimelineObject> {
        TODO("Not yet implemented")
    }

    override suspend fun findByPostId(postId: PostId): List<TimelineObject> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteByPostId(postId: PostId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteByTimelineIdAndActorId(timelineId: TimelineId, actorId: ActorId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteByTimelineId(timelineId: TimelineId) {
        TODO("Not yet implemented")
    }
}