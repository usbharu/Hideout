package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.User
import org.jetbrains.exposed.sql.Query
import org.springframework.stereotype.Component

@Component
class UserQueryMapper(private val userResultRowMapper: ResultRowMapper<User>) : QueryMapper<User> {
    override fun map(query: Query): List<User> = query.map(userResultRowMapper::map)
}
