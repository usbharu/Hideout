package dev.usbharu.hideout.repository

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.service.core.IdGenerateService
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class UserRepositoryImpl(private val idGenerateService: IdGenerateService) :
    UserRepository {

    override suspend fun save(user: User): User {
        val singleOrNull = Users.select { Users.id eq user.id or (Users.url eq user.url) }.empty()
        if (singleOrNull) {
            Users.insert {
                it[id] = user.id
                it[name] = user.name
                it[domain] = user.domain
                it[screenName] = user.screenName
                it[description] = user.description
                it[password] = user.password
                it[inbox] = user.inbox
                it[outbox] = user.outbox
                it[url] = user.url
                it[createdAt] = user.createdAt.toEpochMilli()
                it[publicKey] = user.publicKey
                it[privateKey] = user.privateKey
            }
        } else {
            Users.update({ Users.id eq user.id }) {
                it[name] = user.name
                it[domain] = user.domain
                it[screenName] = user.screenName
                it[description] = user.description
                it[password] = user.password
                it[inbox] = user.inbox
                it[outbox] = user.outbox
                it[url] = user.url
                it[createdAt] = user.createdAt.toEpochMilli()
                it[publicKey] = user.publicKey
                it[privateKey] = user.privateKey
            }
        }
        return user
    }

    override suspend fun findById(id: Long): User? {
        return Users.select { Users.id eq id }.map {
            it.toUser()
        }.singleOrNull()
    }

    override suspend fun deleteFollowRequest(id: Long, follower: Long) {
        FollowRequests.deleteWhere { userId.eq(id) and followerId.eq(follower) }
    }

    override suspend fun findFollowRequestsById(id: Long, follower: Long): Boolean {
        return FollowRequests.select { (FollowRequests.userId eq id) and (FollowRequests.followerId eq follower) }
            .singleOrNull() != null
    }

    override suspend fun delete(id: Long) {
        Users.deleteWhere { Users.id.eq(id) }
    }

    override suspend fun nextId(): Long = idGenerateService.generateId()
}

object Users : Table("users") {
    val id: Column<Long> = long("id")
    val name: Column<String> = varchar("name", length = Config.configData.characterLimit.account.id)
    val domain: Column<String> = varchar("domain", length = Config.configData.characterLimit.general.domain)
    val screenName: Column<String> = varchar("screen_name", length = Config.configData.characterLimit.account.name)
    val description: Column<String> = varchar(
        "description",
        length = Config.configData.characterLimit.account.description
    )
    val password: Column<String?> = varchar("password", length = 255).nullable()
    val inbox: Column<String> = varchar("inbox", length = Config.configData.characterLimit.general.url).uniqueIndex()
    val outbox: Column<String> = varchar("outbox", length = Config.configData.characterLimit.general.url).uniqueIndex()
    val url: Column<String> = varchar("url", length = Config.configData.characterLimit.general.url).uniqueIndex()
    val publicKey: Column<String> = varchar("public_key", length = Config.configData.characterLimit.general.publicKey)
    val privateKey: Column<String?> = varchar(
        "private_key",
        length = Config.configData.characterLimit.general.privateKey
    ).nullable()
    val createdAt: Column<Long> = long("created_at")

    override val primaryKey: PrimaryKey = PrimaryKey(id)

    init {
        uniqueIndex(name, domain)
    }
}

fun ResultRow.toUser(): User {
    return User.of(
        id = this[Users.id],
        name = this[Users.name],
        domain = this[Users.domain],
        screenName = this[Users.screenName],
        description = this[Users.description],
        password = this[Users.password],
        inbox = this[Users.inbox],
        outbox = this[Users.outbox],
        url = this[Users.url],
        publicKey = this[Users.publicKey],
        privateKey = this[Users.privateKey],
        createdAt = Instant.ofEpochMilli((this[Users.createdAt]))
    )
}

object UsersFollowers : LongIdTable("users_followers") {
    val userId: Column<Long> = long("user_id").references(Users.id).index()
    val followerId: Column<Long> = long("follower_id").references(Users.id)

    init {
        uniqueIndex(userId, followerId)
    }
}

object FollowRequests : LongIdTable("follow_requests") {
    val userId: Column<Long> = long("user_id").references(Users.id)
    val followerId: Column<Long> = long("follower_id").references(Users.id)

    init {
        uniqueIndex(userId, followerId)
    }
}
