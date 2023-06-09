package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.service.core.IdGenerateService
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single
import java.time.Instant

@Single
class UserRepository(private val database: Database, private val idGenerateService: IdGenerateService) :
    IUserRepository {
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

    @Suppress("InjectDispatcher")
    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun save(user: User): User {
        return query {
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
            return@query user
        }
    }

    override suspend fun createFollower(id: Long, follower: Long) {
        return query {
            UsersFollowers.insert {
                it[userId] = id
                it[followerId] = follower
            }
        }
    }

    override suspend fun findById(id: Long): User? {
        return query {
            Users.select { Users.id eq id }.map {
                it.toUser()
            }.singleOrNull()
        }
    }

    override suspend fun findByIds(ids: List<Long>): List<User> {
        return query {
            Users.select { Users.id inList ids }.map {
                it.toUser()
            }
        }
    }

    override suspend fun findByName(name: String): List<User> {
        return query {
            Users.select { Users.name eq name }.map {
                it.toUser()
            }
        }
    }

    override suspend fun findByNameAndDomain(name: String, domain: String): User? {
        return query {
            Users.select { Users.name eq name and (Users.domain eq domain) }.singleOrNull()?.toUser()
        }
    }

    override suspend fun findByDomain(domain: String): List<User> {
        return query {
            Users.select { Users.domain eq domain }.map {
                it.toUser()
            }
        }
    }

    override suspend fun findByNameAndDomains(names: List<Pair<String, String>>): List<User> {
        return query {
            val selectAll = Users.selectAll()
            names.forEach { (name, domain) ->
                selectAll.orWhere { Users.name eq name and (Users.domain eq domain) }
            }
            selectAll.map { it.toUser() }
        }
    }

    override suspend fun findByUrl(url: String): User? {
        return query {
            Users.select { Users.url eq url }.singleOrNull()?.toUser()
        }
    }

    override suspend fun findByUrls(urls: List<String>): List<User> {
        return query {
            Users.select { Users.url inList urls }.map { it.toUser() }
        }
    }

    override suspend fun findFollowersById(id: Long): List<User> {
        return query {
            val followers = Users.alias("FOLLOWERS")
            Users.innerJoin(
                otherTable = UsersFollowers,
                onColumn = { Users.id },
                otherColumn = { userId }
            )
                .innerJoin(
                    otherTable = followers,
                    onColumn = { UsersFollowers.followerId },
                    otherColumn = { followers[Users.id] }
                )
                .slice(
                    followers.get(Users.id),
                    followers.get(Users.name),
                    followers.get(Users.domain),
                    followers.get(Users.screenName),
                    followers.get(Users.description),
                    followers.get(Users.password),
                    followers.get(Users.inbox),
                    followers.get(Users.outbox),
                    followers.get(Users.url),
                    followers.get(Users.publicKey),
                    followers.get(Users.privateKey),
                    followers.get(Users.createdAt)
                )
                .select { Users.id eq id }
                .map {
                    User(
                        id = it[followers[Users.id]],
                        name = it[followers[Users.name]],
                        domain = it[followers[Users.domain]],
                        screenName = it[followers[Users.screenName]],
                        description = it[followers[Users.description]],
                        password = it[followers[Users.password]],
                        inbox = it[followers[Users.inbox]],
                        outbox = it[followers[Users.outbox]],
                        url = it[followers[Users.url]],
                        publicKey = it[followers[Users.publicKey]],
                        privateKey = it[followers[Users.privateKey]],
                        createdAt = Instant.ofEpochMilli(it[followers[Users.createdAt]])
                    )
                }
        }
    }

    override suspend fun addFollowRequest(id: Long, follower: Long) {
        query {
            FollowRequests.insert {
                it[userId] = id
                it[followerId] = follower
            }
        }
    }

    override suspend fun deleteFollowRequest(id: Long, follower: Long) {
        query {
            FollowRequests.deleteWhere { userId.eq(id) and followerId.eq(follower) }
        }
    }

    override suspend fun findFollowRequestsById(id: Long, follower: Long): Boolean {
        return query {
            FollowRequests.select { (FollowRequests.userId eq id) and (FollowRequests.followerId eq follower) }
                .singleOrNull() != null
        }
    }

    override suspend fun delete(id: Long) {
        query {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }

    override suspend fun deleteFollower(id: Long, follower: Long) {
        query {
            UsersFollowers.deleteWhere { (userId eq id).and(followerId eq follower) }
        }
    }

    override suspend fun findAll(): List<User> {
        return query {
            Users.selectAll().map { it.toUser() }
        }
    }

    override suspend fun findAllByLimitAndByOffset(limit: Int, offset: Long): List<User> {
        return query {
            Users.selectAll().limit(limit, offset).map { it.toUser() }
        }
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
