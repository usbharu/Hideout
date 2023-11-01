package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Users
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class UserQueryServiceImpl(
    private val userResultRowMapper: ResultRowMapper<User>,
    private val userQueryMapper: QueryMapper<User>
) : UserQueryService {

    private val logger = LoggerFactory.getLogger(UserQueryServiceImpl::class.java)

    override suspend fun findAll(limit: Int, offset: Long): List<User> =
        Users.selectAll().limit(limit, offset).let(userQueryMapper::map)

    override suspend fun findById(id: Long): User = Users.select { Users.id eq id }
        .singleOr { FailedToGetResourcesException("id: $id is duplicate or does not exist.", it) }
        .let(userResultRowMapper::map)

    override suspend fun findByName(name: String): List<User> =
        Users.select { Users.name eq name }.let(userQueryMapper::map)

    override suspend fun findByNameAndDomain(name: String, domain: String): User =
        Users
            .select { Users.name eq name and (Users.domain eq domain) }
            .singleOr {
                FailedToGetResourcesException("name: $name,domain: $domain  is duplicate or does not exist.", it)
            }
            .let(userResultRowMapper::map)

    override suspend fun findByUrl(url: String): User {
        logger.trace("findByUrl url: $url")
        return Users.select { Users.url eq url }
            .singleOr { FailedToGetResourcesException("url: $url  is duplicate or does not exist.", it) }
            .let(userResultRowMapper::map)
    }

    override suspend fun findByIds(ids: List<Long>): List<User> =
        Users.select { Users.id inList ids }.let(userQueryMapper::map)

    override suspend fun existByNameAndDomain(name: String, domain: String): Boolean =
        Users.select { Users.name eq name and (Users.domain eq domain) }.empty().not()

    override suspend fun findByKeyId(keyId: String): User {
        return Users.select { Users.keyId eq keyId }
            .singleOr { FailedToGetResourcesException("keyId: $keyId  is duplicate or does not exist.", it) }
            .let(userResultRowMapper::map)
    }
}
