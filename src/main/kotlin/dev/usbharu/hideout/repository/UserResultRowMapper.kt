package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.User
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
            following = resultRow[Users.following]
        )
    }
}
