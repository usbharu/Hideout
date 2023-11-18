package dev.usbharu.hideout.core.infrastructure.exposed

import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Users
import org.jetbrains.exposed.sql.ResultRow
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class UserResultRowMapper(private val userBuilder: User.UserBuilder) : ResultRowMapper<User> {
    override fun map(resultRow: ResultRow): User {
        return userBuilder.of(
            id = resultRow[Users.id],
            name = resultRow[Users.name],
            domain = resultRow[Users.domain],
            screenName = resultRow[Users.screenName],
            description = resultRow[Users.description],
            password = resultRow[Users.password],
            inbox = resultRow[Users.inbox],
            outbox = resultRow[Users.outbox],
            url = resultRow[Users.url],
            publicKey = resultRow[Users.publicKey],
            privateKey = resultRow[Users.privateKey],
            createdAt = Instant.ofEpochMilli((resultRow[Users.createdAt])),
            keyId = resultRow[Users.keyId],
            followers = resultRow[Users.followers],
            following = resultRow[Users.following],
            instance = resultRow[Users.instance]
        )
    }
}
