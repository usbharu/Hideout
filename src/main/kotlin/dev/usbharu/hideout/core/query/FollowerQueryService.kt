package dev.usbharu.hideout.core.query

import dev.usbharu.hideout.core.domain.model.actor.Actor

@Deprecated("Use RelationshipQueryService")
interface FollowerQueryService {
    suspend fun findFollowersById(id: Long): List<Actor>
    suspend fun alreadyFollow(actorId: Long, followerId: Long): Boolean
}
