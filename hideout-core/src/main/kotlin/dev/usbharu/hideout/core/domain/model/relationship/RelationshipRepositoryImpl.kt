/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.domain.model.relationship

import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.application.infrastructure.exposed.withPagination
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
        val singleOrNull = Relationships.selectAll().where {
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
            (actorId eq relationship.actorId).and(
                targetActorId eq relationship.targetActorId
            )
        }
    }

    override suspend fun findByUserIdAndTargetUserId(actorId: Long, targetActorId: Long): Relationship? = query {
        return@query Relationships.selectAll()
            .where { (Relationships.actorId eq actorId).and(Relationships.targetActorId eq targetActorId) }
            .singleOrNull()?.toRelationships()
    }

    override suspend fun deleteByActorIdOrTargetActorId(actorId: Long, targetActorId: Long): Unit = query {
        Relationships.deleteWhere {
            Relationships.actorId.eq(actorId).or(Relationships.targetActorId.eq(targetActorId))
        }
    }

    override suspend fun findByTargetIdAndFollowing(targetId: Long, following: Boolean): List<Relationship> = query {
        return@query Relationships
            .selectAll().where { Relationships.targetActorId eq targetId and (Relationships.following eq following) }
            .map { it.toRelationships() }
    }

    override suspend fun countByTargetIdAndFollowing(targetId: Long, following: Boolean): Int = query {
        return@query Relationships
            .selectAll()
            .where {
                Relationships.targetActorId eq targetId and (Relationships.following eq following)
            }
            .count()
            .toInt()
    }

    override suspend fun countByUserIdAndFollowing(userId: Long, following: Boolean): Int = query {
        return@query Relationships
            .selectAll()
            .where {
                Relationships.actorId eq userId and (Relationships.following eq following)
            }
            .count()
            .toInt()
    }

    override suspend fun findByTargetIdAndFollowRequestAndIgnoreFollowRequest(
        targetId: Long,
        followRequest: Boolean,
        ignoreFollowRequest: Boolean,
        page: Page.PageByMaxId,
    ): PaginationList<Relationship, Long> = query {
        val query = Relationships.selectAll().where {
            Relationships.targetActorId.eq(targetId).and(Relationships.followRequest.eq(followRequest))
                .and(Relationships.ignoreFollowRequestFromTarget.eq(ignoreFollowRequest))
        }

        val resultRowList = query.withPagination(page, Relationships.id)

        return@query PaginationList(
            resultRowList.map { it.toRelationships() },
            resultRowList.next?.value,
            resultRowList.prev?.value
        )
    }

    override suspend fun findByActorIdAndMuting(
        actorId: Long,
        muting: Boolean,
        page: Page.PageByMaxId,
    ): PaginationList<Relationship, Long> = query {
        val query =
            Relationships.selectAll().where { Relationships.actorId.eq(actorId).and(Relationships.muting.eq(muting)) }

        val resultRowList = query.withPagination(page, Relationships.id)

        return@query PaginationList(
            resultRowList.map { it.toRelationships() },
            resultRowList.next?.value,
            resultRowList.prev?.value
        )
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
