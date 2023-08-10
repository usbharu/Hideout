package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.repository.Users
import dev.usbharu.hideout.repository.toUser
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class UserQueryServiceImpl : UserQueryService {
    override suspend fun findById(id: Long): User = Users.select { Users.id eq id }.single().toUser()

    override suspend fun findByName(name: String): List<User> {
        return Users.select { Users.name eq name }.map { it.toUser() }
    }

    override suspend fun findByNameAndDomain(name: String, domain: String): User {
        return Users.select { Users.name eq name and (Users.domain eq domain) }.single().toUser()
    }

    override suspend fun findByUrl(url: String): User {
        return Users.select { Users.url eq url }.single().toUser()
    }
}
