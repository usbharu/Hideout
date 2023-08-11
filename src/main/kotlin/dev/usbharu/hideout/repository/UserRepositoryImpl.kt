package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.service.core.IdGenerateService
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single
import java.time.Instant

@Single
class UserRepositoryImpl(private val database: Database, private val idGenerateService: IdGenerateService) :
    UserRepository {
    init {
        transaction(database) {
            SchemaUtils.create(Users)
            SchemaUtils.create(UsersFollowers)
            SchemaUtils.createMissingTablesAndColumns(Users)
            SchemaUtils.createMissingTablesAndColumns(UsersFollowers)
            SchemaUtils.create(FollowRequests)
            SchemaUtils.createMissingTablesAndColumns(FollowRequests)
        }
    }

    override suspend fun save(user: User): User {
        val singleOrNull = Users.select { Users.id eq user.id }.singleOrNull()
        if (singleOrNull == null) {
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
    val id = long("id")
    val name = varchar("name", length = 64)
    val domain = varchar("domain", length = 255)
    val screenName = varchar("screen_name", length = 64)
    val description = varchar("description", length = 600)
    val password = varchar("password", length = 255).nullable()
    val inbox = varchar("inbox", length = 255).uniqueIndex()
    val outbox = varchar("outbox", length = 255).uniqueIndex()
    val url = varchar("url", length = 255).uniqueIndex()
    val publicKey = varchar("public_key", length = 10000)
    val privateKey = varchar("private_key", length = 10000).nullable()
    val createdAt = long("created_at")

    override val primaryKey: PrimaryKey = PrimaryKey(id)

    init {
        uniqueIndex(name, domain)
    }
}

fun ResultRow.toUser(): User {
    return User(
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
    val userId = long("user_id").references(Users.id).index()
    val followerId = long("follower_id").references(Users.id)

    init {
        uniqueIndex(userId, followerId)
    }
}

object FollowRequests : LongIdTable("follow_requests") {
    val userId = long("user_id").references(Users.id)
    val followerId = long("follower_id").references(Users.id)

    init {
        uniqueIndex(userId, followerId)
    }
}
