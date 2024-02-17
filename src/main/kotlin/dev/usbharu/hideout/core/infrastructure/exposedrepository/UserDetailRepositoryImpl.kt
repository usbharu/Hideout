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

package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class UserDetailRepositoryImpl : UserDetailRepository, AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun save(userDetail: UserDetail): UserDetail = query {
        val singleOrNull =
            UserDetails.selectAll().where { UserDetails.actorId eq userDetail.actorId }.forUpdate().singleOrNull()
        if (singleOrNull == null) {
            UserDetails.insert {
                it[actorId] = userDetail.actorId
                it[password] = userDetail.password
                it[autoAcceptFolloweeFollowRequest] = userDetail.autoAcceptFolloweeFollowRequest
            }
        } else {
            UserDetails.update({ UserDetails.actorId eq userDetail.actorId }) {
                it[password] = userDetail.password
                it[autoAcceptFolloweeFollowRequest] = userDetail.autoAcceptFolloweeFollowRequest
            }
        }
        return@query userDetail
    }

    override suspend fun delete(userDetail: UserDetail): Unit = query {
        UserDetails.deleteWhere { actorId eq userDetail.actorId }
    }

    override suspend fun findByActorId(actorId: Long): UserDetail? = query {
        return@query UserDetails
            .selectAll().where { UserDetails.actorId eq actorId }
            .singleOrNull()
            ?.let {
                UserDetail(
                    it[UserDetails.actorId],
                    it[UserDetails.password],
                    it[UserDetails.autoAcceptFolloweeFollowRequest]
                )
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserDetailRepositoryImpl::class.java)
    }
}

object UserDetails : LongIdTable("user_details") {
    val actorId = long("actor_id").references(Actors.id)
    val password = varchar("password", 255)
    val autoAcceptFolloweeFollowRequest = bool("auto_accept_followee_follow_request")
}
