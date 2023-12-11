package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ActorQueryServiceImpl(
    private val actorResultRowMapper: ResultRowMapper<Actor>,
    private val actorQueryMapper: QueryMapper<Actor>
) : ActorQueryService {

    private val logger = LoggerFactory.getLogger(ActorQueryServiceImpl::class.java)

    override suspend fun findAll(limit: Int, offset: Long): List<Actor> =
        Actors.selectAll().limit(limit, offset).let(actorQueryMapper::map)

    override suspend fun findById(id: Long): Actor = Actors.select { Actors.id eq id }
        .singleOr { FailedToGetResourcesException("id: $id is duplicate or does not exist.", it) }
        .let(actorResultRowMapper::map)

    override suspend fun findByName(name: String): List<Actor> =
        Actors.select { Actors.name eq name }.let(actorQueryMapper::map)

    override suspend fun findByNameAndDomain(name: String, domain: String): Actor =
        Actors
            .select { Actors.name eq name and (Actors.domain eq domain) }
            .singleOr {
                FailedToGetResourcesException("name: $name,domain: $domain  is duplicate or does not exist.", it)
            }
            .let(actorResultRowMapper::map)

    override suspend fun findByUrl(url: String): Actor {
        logger.trace("findByUrl url: $url")
        return Actors.select { Actors.url eq url }
            .singleOr { FailedToGetResourcesException("url: $url  is duplicate or does not exist.", it) }
            .let(actorResultRowMapper::map)
    }

    override suspend fun findByIds(ids: List<Long>): List<Actor> =
        Actors.select { Actors.id inList ids }.let(actorQueryMapper::map)

    override suspend fun existByNameAndDomain(name: String, domain: String): Boolean =
        Actors.select { Actors.name eq name and (Actors.domain eq domain) }.empty().not()

    override suspend fun findByKeyId(keyId: String): Actor {
        return Actors.select { Actors.keyId eq keyId }
            .singleOr { FailedToGetResourcesException("keyId: $keyId  is duplicate or does not exist.", it) }
            .let(actorResultRowMapper::map)
    }
}
