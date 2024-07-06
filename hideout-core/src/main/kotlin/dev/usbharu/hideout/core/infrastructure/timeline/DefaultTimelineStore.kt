package dev.usbharu.hideout.core.infrastructure.timeline

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.relationship.FindRelationshipOption
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository

open class DefaultTimelineStore(
    private val timelineRepository: TimelineRepository,
    private val relationshipRepository: RelationshipRepository
) : AbstractTimelineStore() {
    override suspend fun getFollowers(actorId: ActorId): List<ActorId> {
        return relationshipRepository
            .findByTargetId(
                actorId, FindRelationshipOption(follow = true, mute = false),
                FindRelationshipOption(block = false)
            )
            .map { it.actorId }
    }
}