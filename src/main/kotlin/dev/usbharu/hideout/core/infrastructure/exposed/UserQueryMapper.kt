package dev.usbharu.hideout.core.infrastructure.exposed

import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.core.domain.model.actor.Actor
import org.jetbrains.exposed.sql.Query
import org.springframework.stereotype.Component

@Component
class UserQueryMapper(private val actorResultRowMapper: ResultRowMapper<Actor>) : QueryMapper<Actor> {
    override fun map(query: Query): List<Actor> = query.map(actorResultRowMapper::map)
}
