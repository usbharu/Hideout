package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.timeline.*
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.domain.shared.repository.DomainEventPublishableRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedTimelineRepository(override val domainEventPublisher: DomainEventPublisher) : TimelineRepository,
    AbstractRepository(), DomainEventPublishableRepository<Timeline> {
    override suspend fun save(timeline: Timeline): Timeline {
        query {
            Timelines.insert {
                it[id] = timeline.id.value
                it[userDetailId] = timeline.userDetailId.id
                it[name] = timeline.name.value
                it[visibility] = timeline.visibility.name
                it[isSystem] = timeline.isSystem
            }
        }
        update(timeline)
        return timeline
    }

    override suspend fun delete(timeline: Timeline) {
        query {
            Timelines.deleteWhere {
                Timelines.id eq timeline.id.value
            }
        }
        update(timeline)
    }

    override suspend fun findByIds(ids: List<TimelineId>): List<Timeline> {
        return query {
            Timelines.selectAll().where { Timelines.id inList ids.map { it.value } }.map { it.toTimeline() }
        }
    }

    override suspend fun findById(id: TimelineId): Timeline? {
        return query {
            Timelines.selectAll().where { Timelines.id eq id.value }.firstOrNull()?.toTimeline()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedTimelineRepository::class.java.name)
    }

    override val logger: Logger
        get() = Companion.logger

}

fun ResultRow.toTimeline(): Timeline {
    return Timeline(
        TimelineId(this[Timelines.id]),
        UserDetailId(this[Timelines.userDetailId]),
        TimelineName(this[Timelines.name]),
        TimelineVisibility.valueOf(this[Timelines.visibility]),
        this[Timelines.isSystem]
    )
}

object Timelines : Table("timelines") {
    val id = long("id")
    val userDetailId = long("user_detail_id").references(UserDetails.id)
    val name = varchar("name", 300)
    val visibility = varchar("visibility", 100)
    val isSystem = bool("is_system")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}