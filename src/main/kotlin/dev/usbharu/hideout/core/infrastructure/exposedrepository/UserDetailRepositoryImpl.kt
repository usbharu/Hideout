package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

@Repository
class UserDetailRepositoryImpl : UserDetailRepository {
    override suspend fun save(userDetail: UserDetail): UserDetail {
        val singleOrNull = UserDetails.select { UserDetails.actorId eq userDetail.actorId }.singleOrNull()
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
        return userDetail
    }

    override suspend fun delete(userDetail: UserDetail) {
        UserDetails.deleteWhere { UserDetails.actorId eq userDetail.actorId }
    }

    override suspend fun findByActorId(actorId: Long): UserDetail? {
        return UserDetails
            .select { UserDetails.actorId eq actorId }
            .singleOrNull()
            ?.let {
                UserDetail(
                    it[UserDetails.actorId],
                    it[UserDetails.password],
                    it[UserDetails.autoAcceptFolloweeFollowRequest]
                )
            }
    }

}


object UserDetails : LongIdTable("user_details") {
    val actorId = long("actor_id").references(Actors.id)
    val password = varchar("password", 255)
    val autoAcceptFolloweeFollowRequest = bool("auto_accept_followee_follow_request")
}
