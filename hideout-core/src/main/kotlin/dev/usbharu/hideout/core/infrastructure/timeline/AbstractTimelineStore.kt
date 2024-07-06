package dev.usbharu.hideout.core.infrastructure.timeline

import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.relationship.FindRelationshipOption
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.external.timeline.TimelineStore

abstract class AbstractTimelineStore(
    private val timelineRepository: TimelineRepository,
    private val relationshipRepository: RelationshipRepository
) :
    TimelineStore {
    override suspend fun newPost(post: Post) {
        relationshipRepository.findByTargetId(post.actorId, FindRelationshipOption(follow = true, mute = false))
    }


}