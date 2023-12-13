package dev.usbharu.hideout.core.query

import dev.usbharu.hideout.core.domain.model.relationship.Relationship

interface RelationshipQueryService {

    suspend fun findByTargetIdAndFollowing(targetId: Long, following: Boolean): List<Relationship>

    @Suppress("LongParameterList", "FunctionMaxLength")
    suspend fun findByTargetIdAndFollowRequestAndIgnoreFollowRequest(
        maxId: Long?,
        sinceId: Long?,
        limit: Int,
        targetId: Long,
        followRequest: Boolean,
        ignoreFollowRequest: Boolean
    ): List<Relationship>
}
