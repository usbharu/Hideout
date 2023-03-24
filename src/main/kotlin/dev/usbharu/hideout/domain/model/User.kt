package dev.usbharu.hideout.domain.model

import org.jetbrains.exposed.dao.id.LongIdTable

data class User(val name: String, val screenName: String, val description: String)

data class UserEntity(
    val id: Long,
    val name: String,
    val screenName: String,
    val description: String
) {
    constructor(id: Long, user: User) : this(id, user.name, user.screenName, user.description)
}

object Users : LongIdTable("users") {
    val name = varchar("name", length = 64).uniqueIndex()
    val screenName = varchar("screen_name", length = 64)
    val description = varchar("description", length = 600)
}
