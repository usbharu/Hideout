package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipId
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipRepository
import dev.usbharu.hideout.core.domain.model.timelinerelationship.Visible
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedTimelineRelationshipRepository : AbstractRepository(), TimelineRelationshipRepository {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun save(timelineRelationship: TimelineRelationship): TimelineRelationship {
        query {
            TimelineRelationships.upsert {
                it[id] = timelineRelationship.id.value
                it[timelineId] = timelineRelationship.timelineId.value
                it[actorId] = timelineRelationship.actorId.id
                it[visible] = timelineRelationship.visible.name
            }
        }
        return timelineRelationship
    }

    override suspend fun delete(timelineRelationship: TimelineRelationship) {
        query {
            TimelineRelationships.deleteWhere {
                TimelineRelationships.id eq timelineRelationship.id.value
            }
        }
    }

    override suspend fun findByActorId(actorId: ActorId): List<TimelineRelationship> {
        return query {
            TimelineRelationships.selectAll().where {
                TimelineRelationships.actorId eq actorId.id
            }.map { it.toTimelineRelationship() }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedTimelineRelationshipRepository::class.java)
    }
}

fun ResultRow.toTimelineRelationship(): TimelineRelationship {
    return TimelineRelationship(
        TimelineRelationshipId(this[TimelineRelationships.id]),
        TimelineId(this[TimelineRelationships.timelineId]),
        ActorId(this[TimelineRelationships.actorId]),
        Visible.valueOf(this[TimelineRelationships.visible])
    )
}

object TimelineRelationships : Table("timeline_relationships") {
    val id = long("id")
    val timelineId = long("timeline_id").references(Timelines.id)
    val actorId = long("actor_id").references(Actors.id)
    val visible = varchar("visible", 100)
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
