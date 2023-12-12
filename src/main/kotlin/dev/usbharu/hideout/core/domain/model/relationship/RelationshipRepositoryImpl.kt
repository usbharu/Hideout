package dev.usbharu.hideout.core.domain.model.relationship

import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Service

@Service
class RelationshipRepositoryImpl : RelationshipRepository {
    override suspend fun save(relationship: Relationship): Relationship {
        val singleOrNull =
            Relationships
                .select {
                    (Relationships.actorId eq relationship.actorId)
                        .and(Relationships.targetActorId eq relationship.targetActorId)
                }
                .singleOrNull()

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
            Relationships
                .update({
                    (Relationships.actorId eq relationship.actorId)
                        .and(Relationships.targetActorId eq relationship.targetActorId)
                }) {
                    it[following] = relationship.following
                    it[blocking] = relationship.blocking
                    it[muting] = relationship.muting
                    it[followRequest] = relationship.followRequest
                    it[ignoreFollowRequestFromTarget] = relationship.ignoreFollowRequestToTarget
                }
        }
        return relationship
    }

    override suspend fun delete(relationship: Relationship) {
        Relationships.deleteWhere {
            (Relationships.actorId eq relationship.actorId)
                .and(Relationships.targetActorId eq relationship.targetActorId)
        }
    }

    override suspend fun findByUserIdAndTargetUserId(actorId: Long, targetActorId: Long): Relationship? {
        return Relationships.select {
            (Relationships.actorId eq actorId)
                .and(Relationships.targetActorId eq targetActorId)
        }.singleOrNull()
            ?.toRelationships()
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
