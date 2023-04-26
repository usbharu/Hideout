package dev.usbharu.hideout.domain.model

import dev.usbharu.hideout.repository.Users
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

@Deprecated("")
data class UserAuthentication(
    val userId: Long,
    val hash: String?,
    val publicKey: String,
    val privateKey: String?
)
@Deprecated("")
data class UserAuthenticationEntity(
    val id: Long,
    val userId: Long,
    val hash: String?,
    val publicKey: String,
    val privateKey: String?
) {
    constructor(id: Long, userAuthentication: UserAuthentication) : this(
        id,
        userAuthentication.userId,
        userAuthentication.hash,
        userAuthentication.publicKey,
        userAuthentication.privateKey
    )
}
@Deprecated("")
object UsersAuthentication : LongIdTable("users_auth") {
    val userId = long("user_id").references(Users.id, onUpdate = ReferenceOption.CASCADE)
    val hash = varchar("hash", length = 64).nullable()
    val publicKey = varchar("public_key", length = 1000_000)
    val privateKey = varchar("private_key", length = 1000_000).nullable()
}
