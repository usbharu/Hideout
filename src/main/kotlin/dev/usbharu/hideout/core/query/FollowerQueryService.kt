package dev.usbharu.hideout.core.query

import dev.usbharu.hideout.core.domain.model.user.User

@Deprecated("Use RelationshipQueryService")
interface FollowerQueryService {
    suspend fun findFollowersById(id: Long): List<User>
    suspend fun alreadyFollow(userId: Long, followerId: Long): Boolean
}
