package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.domain.model.user.UserRepository
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val idGenerateService: IdGenerateService,
    private val userResultRowMapper: ResultRowMapper<User>
) :
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
                it[keyId] = user.keyId
                it[following] = user.following
                it[followers] = user.followers
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
                it[keyId] = user.keyId
                it[following] = user.following
                it[followers] = user.followers
            }
        }
        return user
    }

    override suspend fun findById(id: Long): User? =
        Users.select { Users.id eq id }.singleOrNull()?.let(userResultRowMapper::map)

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
    val name: Column<String> = varchar("name", length = 300)
    val domain: Column<String> = varchar("domain", length = 1000)
    val screenName: Column<String> = varchar("screen_name", length = 300)
    val description: Column<String> = varchar(
        "description",
        length = 10000
    )
    val password: Column<String?> = varchar("password", length = 255).nullable()
    val inbox: Column<String> = varchar("inbox", length = 1000).uniqueIndex()
    val outbox: Column<String> = varchar("outbox", length = 1000).uniqueIndex()
    val url: Column<String> = varchar("url", length = 1000).uniqueIndex()
    val publicKey: Column<String> = varchar("public_key", length = 10000)
    val privateKey: Column<String?> = varchar(
        "private_key",
        length = 10000
    ).nullable()
    val createdAt: Column<Long> = long("created_at")
    val keyId = varchar("key_id", length = 1000)
    val following = varchar("following", length = 1000).nullable()
    val followers = varchar("followers", length = 1000).nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)

    init {
        uniqueIndex(name, domain)
    }
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
