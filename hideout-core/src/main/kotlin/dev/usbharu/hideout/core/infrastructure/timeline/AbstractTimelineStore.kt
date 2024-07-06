package dev.usbharu.hideout.core.infrastructure.timeline

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.external.timeline.TimelineStore

abstract class AbstractTimelineStore() : TimelineStore {
    override suspend fun newPost(post: Post) {
        getFollowers(post.actorId)
    }

    protected abstract suspend fun getFollowers(actorId: ActorId): List<ActorId>

}