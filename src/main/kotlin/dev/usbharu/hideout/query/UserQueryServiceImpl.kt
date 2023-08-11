package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.repository.Users
import dev.usbharu.hideout.repository.toUser
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single

@Single
class UserQueryServiceImpl : UserQueryService {
    override suspend fun findAll(limit: Int, offset: Long): List<User> =
        Users.selectAll().limit(limit, offset).map { it.toUser() }

    override suspend fun findById(id: Long): User = Users.select { Users.id eq id }.single().toUser()

    override suspend fun findByName(name: String): List<User> = Users.select { Users.name eq name }.map { it.toUser() }

    override suspend fun findByNameAndDomain(name: String, domain: String): User =
        Users.select { Users.name eq name and (Users.domain eq domain) }.single().toUser()

    override suspend fun findByUrl(url: String): User = Users.select { Users.url eq url }.single().toUser()

    override suspend fun findByIds(ids: List<Long>): List<User> =
        Users.select { Users.id inList ids }.map { it.toUser() }

    override suspend fun existByNameAndDomain(name: String, domain: String): Boolean =
        Users.select { Users.name eq name and (Users.domain eq domain) }.empty().not()
}
