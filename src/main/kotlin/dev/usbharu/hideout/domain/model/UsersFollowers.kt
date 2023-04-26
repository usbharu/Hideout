package dev.usbharu.hideout.domain.model

import dev.usbharu.hideout.repository.Users
import org.jetbrains.exposed.dao.id.LongIdTable

object UsersFollowers : LongIdTable("users_followers") {
    val userId = long("user_id").references(Users.id).index()
    val followerId = long("follower_id").references(Users.id)

    init {
        uniqueIndex(userId, followerId)
    }
}
