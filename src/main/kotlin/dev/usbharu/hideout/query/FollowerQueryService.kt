package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.User
import org.springframework.stereotype.Repository

@Repository
interface FollowerQueryService {
    suspend fun findFollowersById(id: Long): List<User>
    suspend fun findFollowersByNameAndDomain(name: String, domain: String): List<User>
    suspend fun findFollowingById(id: Long): List<User>
    suspend fun findFollowingByNameAndDomain(name: String, domain: String): List<User>
    suspend fun appendFollower(user: Long, follower: Long)
    suspend fun removeFollower(user: Long, follower: Long)
    suspend fun alreadyFollow(userId: Long, followerId: Long): Boolean
}
