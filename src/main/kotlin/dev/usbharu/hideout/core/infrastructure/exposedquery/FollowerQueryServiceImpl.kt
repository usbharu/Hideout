package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.query.RelationshipQueryService
import dev.usbharu.hideout.core.query.UserQueryService
import org.springframework.stereotype.Repository

@Repository
class FollowerQueryServiceImpl(
    private val relationshipQueryService: RelationshipQueryService,
    private val userQueryService: UserQueryService,
    private val relationshipRepository: RelationshipRepository
) : FollowerQueryService {
    override suspend fun findFollowersById(id: Long): List<User> {
        return userQueryService.findByIds(
            relationshipQueryService.findByTargetIdAndFollowing(id, true).map { it.userId }
        )
    }

    override suspend fun alreadyFollow(userId: Long, followerId: Long): Boolean =
        relationshipRepository.findByUserIdAndTargetUserId(followerId, userId)?.following ?: false
}
