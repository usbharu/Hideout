package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.relationship.Relationships
import dev.usbharu.hideout.core.domain.model.relationship.toRelationships
import dev.usbharu.hideout.core.query.RelationshipQueryService
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Service

@Service
class RelationshipQueryServiceImpl : RelationshipQueryService {
    override suspend fun findByTargetIdAndFollowing(targetId: Long, following: Boolean): List<Relationship> =
        Relationships.select { Relationships.targetActorId eq targetId and (Relationships.following eq following) }
            .map { it.toRelationships() }

    override suspend fun findByTargetIdAndFollowRequestAndIgnoreFollowRequest(
        maxId: Long?,
        sinceId: Long?,
        limit: Int,
        targetId: Long,
        followRequest: Boolean,
        ignoreFollowRequest: Boolean
    ): List<Relationship> {
        val query = Relationships
            .select {
                Relationships.targetActorId.eq(targetId)
                    .and(Relationships.followRequest.eq(followRequest))
                    .and(Relationships.ignoreFollowRequestFromTarget.eq(ignoreFollowRequest))
            }.limit(limit)

        if (maxId != null) {
            query.andWhere { Relationships.id greater maxId }
        }

        if (sinceId != null) {
            query.andWhere { Relationships.id less sinceId }
        }

        return query.map { it.toRelationships() }
    }
}
