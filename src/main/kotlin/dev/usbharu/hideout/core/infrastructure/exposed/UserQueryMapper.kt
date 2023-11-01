package dev.usbharu.hideout.core.infrastructure.exposed

import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.core.domain.model.user.User
import org.jetbrains.exposed.sql.Query
import org.springframework.stereotype.Component

@Component
class UserQueryMapper(private val userResultRowMapper: ResultRowMapper<User>) : QueryMapper<User> {
    override fun map(query: Query): List<User> = query.map(userResultRowMapper::map)
}
