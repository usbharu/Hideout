package dev.usbharu.hideout.core.domain.model.relationship

import dev.usbharu.hideout.core.infrastructure.exposedrepository.Users
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
                    (Relationships.userId eq relationship.userId)
                        .and(Relationships.targetUserId eq relationship.targetUserId)
                }
                .singleOrNull()

        if (singleOrNull == null) {
            Relationships.insert {
                it[userId] = relationship.userId
                it[targetUserId] = relationship.targetUserId
                it[following] = relationship.following
                it[blocking] = relationship.blocking
                it[muting] = relationship.blocking
                it[followRequest] = relationship.followRequest
                it[ignoreFollowRequestFromTarget] = relationship.ignoreFollowRequestFromTarget
            }
        } else {
            Relationships
                .update({
                    (Relationships.userId eq relationship.userId)
                        .and(Relationships.targetUserId eq relationship.targetUserId)
                }) {
                    it[following] = relationship.following
                    it[blocking] = relationship.blocking
                    it[muting] = relationship.blocking
                    it[followRequest] = relationship.followRequest
                    it[ignoreFollowRequestFromTarget] = relationship.ignoreFollowRequestFromTarget
                }
        }
        return relationship
    }

    override suspend fun delete(relationship: Relationship) {
        Relationships.deleteWhere {
            (Relationships.userId eq relationship.userId)
                .and(Relationships.targetUserId eq relationship.targetUserId)
        }
    }

    override suspend fun findByUserIdAndTargetUserId(userId: Long, targetUserId: Long): Relationship? {
        return Relationships.select {
            (Relationships.userId eq userId)
                .and(Relationships.targetUserId eq targetUserId)
        }.singleOrNull()
            ?.toRelationships()
    }

}

fun ResultRow.toRelationships(): Relationship = Relationship(
    this[Relationships.userId],
    this[Relationships.targetUserId],
    this[Relationships.following],
    this[Relationships.blocking],
    this[Relationships.muting],
    this[Relationships.followRequest],
    this[Relationships.ignoreFollowRequestFromTarget]
)

object Relationships : LongIdTable("relationships") {
    val userId = long("user_id").references(Users.id)
    val targetUserId = long("target_user_id").references(Users.id)
    val following = bool("following")
    val blocking = bool("blocking")
    val muting = bool("muting")
    val followRequest = bool("follow_request")
    val ignoreFollowRequestFromTarget = bool("ignore_follow_request")

    init {
        uniqueIndex(userId, targetUserId)
    }
}
