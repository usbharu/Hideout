package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.query.RelationshipQueryService
import org.springframework.stereotype.Repository

@Repository
class FollowerQueryServiceImpl(
    private val relationshipQueryService: RelationshipQueryService,
    private val actorQueryService: ActorQueryService,
    private val relationshipRepository: RelationshipRepository
) : FollowerQueryService {
    override suspend fun findFollowersById(id: Long): List<Actor> {
        return actorQueryService.findByIds(
            relationshipQueryService.findByTargetIdAndFollowing(id, true).map { it.actorId }
        )
    }

    override suspend fun alreadyFollow(actorId: Long, followerId: Long): Boolean =
        relationshipRepository.findByUserIdAndTargetUserId(followerId, actorId)?.following ?: false
}
