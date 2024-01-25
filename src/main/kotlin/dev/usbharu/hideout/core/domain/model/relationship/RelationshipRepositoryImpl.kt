package dev.usbharu.hideout.core.domain.model.relationship

import dev.usbharu.hideout.core.infrastructure.exposedrepository.AbstractRepository
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RelationshipRepositoryImpl : RelationshipRepository, AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun save(relationship: Relationship): Relationship = query {
        val singleOrNull = Relationships.select {
            (Relationships.actorId eq relationship.actorId).and(
                Relationships.targetActorId eq relationship.targetActorId
            )
        }.forUpdate().singleOrNull()

        if (singleOrNull == null) {
            Relationships.insert {
                it[actorId] = relationship.actorId
                it[targetActorId] = relationship.targetActorId
                it[following] = relationship.following
                it[blocking] = relationship.blocking
                it[muting] = relationship.muting
                it[followRequest] = relationship.followRequest
                it[ignoreFollowRequestFromTarget] = relationship.ignoreFollowRequestToTarget
            }
        } else {
            Relationships.update({
                (Relationships.actorId eq relationship.actorId).and(
                    Relationships.targetActorId eq relationship.targetActorId
                )
            }) {
                it[following] = relationship.following
                it[blocking] = relationship.blocking
                it[muting] = relationship.muting
                it[followRequest] = relationship.followRequest
                it[ignoreFollowRequestFromTarget] = relationship.ignoreFollowRequestToTarget
            }
        }
        return@query relationship
    }

    override suspend fun delete(relationship: Relationship): Unit = query {
        Relationships.deleteWhere {
            (Relationships.actorId eq relationship.actorId).and(
                Relationships.targetActorId eq relationship.targetActorId
            )
        }
    }

    override suspend fun findByUserIdAndTargetUserId(actorId: Long, targetActorId: Long): Relationship? = query {
        return@query Relationships.select {
            (Relationships.actorId eq actorId).and(Relationships.targetActorId eq targetActorId)
        }.singleOrNull()?.toRelationships()
    }

    override suspend fun deleteByActorIdOrTargetActorId(actorId: Long, targetActorId: Long): Unit = query {
        Relationships.deleteWhere {
            Relationships.actorId.eq(actorId).or(Relationships.targetActorId.eq(targetActorId))
        }
    }

    override suspend fun findByTargetIdAndFollowing(targetId: Long, following: Boolean): List<Relationship> = query {
        return@query Relationships
            .select { Relationships.targetActorId eq targetId and (Relationships.following eq following) }
            .map { it.toRelationships() }
    }

    override suspend fun findByTargetIdAndFollowRequestAndIgnoreFollowRequest(
        maxId: Long?,
        sinceId: Long?,
        limit: Int,
        targetId: Long,
        followRequest: Boolean,
        ignoreFollowRequest: Boolean
    ): List<Relationship> = query {
        val query = Relationships.select {
            Relationships.targetActorId.eq(targetId).and(Relationships.followRequest.eq(followRequest))
                .and(Relationships.ignoreFollowRequestFromTarget.eq(ignoreFollowRequest))
        }.limit(limit)

        if (maxId != null) {
            query.andWhere { Relationships.id lessEq maxId }
        }

        if (sinceId != null) {
            query.andWhere { Relationships.id greaterEq sinceId }
        }

        return@query query.map { it.toRelationships() }
    }

    override suspend fun findByActorIdAntMutingAndMaxIdAndSinceId(
        actorId: Long,
        muting: Boolean,
        maxId: Long?,
        sinceId: Long?,
        limit: Int
    ): List<Relationship> = query {
        val query = Relationships.select {
            Relationships.actorId.eq(actorId).and(Relationships.muting.eq(muting))
        }.limit(limit)

        if (maxId != null) {
            query.andWhere { Relationships.id lessEq maxId }
        }

        if (sinceId != null) {
            query.andWhere { Relationships.id greaterEq sinceId }
        }

        return@query query.map { it.toRelationships() }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RelationshipRepositoryImpl::class.java)
    }
}

fun ResultRow.toRelationships(): Relationship = Relationship(
    actorId = this[Relationships.actorId],
    targetActorId = this[Relationships.targetActorId],
    following = this[Relationships.following],
    blocking = this[Relationships.blocking],
    muting = this[Relationships.muting],
    followRequest = this[Relationships.followRequest],
    ignoreFollowRequestToTarget = this[Relationships.ignoreFollowRequestFromTarget]
)

object Relationships : LongIdTable("relationships") {
    val actorId = long("actor_id").references(Actors.id)
    val targetActorId = long("target_actor_id").references(Actors.id)
    val following = bool("following")
    val blocking = bool("blocking")
    val muting = bool("muting")
    val followRequest = bool("follow_request")
    val ignoreFollowRequestFromTarget = bool("ignore_follow_request")

    init {
        uniqueIndex(actorId, targetActorId)
    }
}
