package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.repository.Users.createdAt
import dev.usbharu.hideout.repository.Users.description
import dev.usbharu.hideout.repository.Users.domain
import dev.usbharu.hideout.repository.Users.id
import dev.usbharu.hideout.repository.Users.inbox
import dev.usbharu.hideout.repository.Users.name
import dev.usbharu.hideout.repository.Users.outbox
import dev.usbharu.hideout.repository.Users.password
import dev.usbharu.hideout.repository.Users.privateKey
import dev.usbharu.hideout.repository.Users.publicKey
import dev.usbharu.hideout.repository.Users.screenName
import dev.usbharu.hideout.repository.Users.url
import dev.usbharu.hideout.repository.UsersFollowers.followerId
import dev.usbharu.hideout.service.IdGenerateService
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
        }
    }

    @Suppress("InjectDispatcher")
    override suspend fun <T> transaction(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun save(user: User): User {
        val singleOrNull = Users.select { id eq user.id }.singleOrNull()
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
            Users.update({ id eq user.id }) {
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

    override suspend fun createFollower(id: Long, follower: Long) {
        UsersFollowers.insert {
            it[userId] = id
            it[followerId] = follower
        }
    }

    override suspend fun findById(id: Long): User? {
        return Users.select { Users.id eq id }.map {
            it.toUser()
        }.singleOrNull()
    }

    override suspend fun findByIds(ids: List<Long>): List<User> {
        return Users.select { id inList ids }.map {
            it.toUser()
        }
    }

    override suspend fun findByName(name: String): List<User> {
        return Users.select { Users.name eq name }.map {
            it.toUser()
        }
    }

    override suspend fun findByNameAndDomain(name: String, domain: String): User? =
        Users.select { Users.name eq name and (Users.domain eq domain) }.singleOrNull()?.toUser()

    override suspend fun findByDomain(domain: String): List<User> {
        return Users.select { Users.domain eq domain }.map {
            it.toUser()
        }
    }

    override suspend fun findByNameAndDomains(names: List<Pair<String, String>>): List<User> {
        val selectAll = Users.selectAll()
        names.forEach { (name, domain) ->
            selectAll.orWhere { Users.name eq name and (Users.domain eq domain) }
        }
        return selectAll.map { it.toUser() }
    }

    override suspend fun findByUrl(url: String): User? = Users.select { Users.url eq url }.singleOrNull()?.toUser()

    override suspend fun findByUrls(urls: List<String>): List<User> =
        Users.select { url inList urls }.map { it.toUser() }

    override suspend fun findFollowersById(id: Long): List<User> {
        val followers = Users.alias("FOLLOWERS")
        return Users.innerJoin(
            otherTable = UsersFollowers,
            onColumn = { Users.id },
            otherColumn = { userId }
        )
            .innerJoin(
                otherTable = followers,
                onColumn = { followerId },
                otherColumn = { followers[Users.id] }
            )
            .slice(
                followers.get(Users.id),
                followers.get(name),
                followers.get(domain),
                followers.get(screenName),
                followers.get(description),
                followers.get(password),
                followers.get(inbox),
                followers.get(outbox),
                followers.get(url),
                followers.get(publicKey),
                followers.get(privateKey),
                followers.get(createdAt)
            )
            .select { Users.id eq id }
            .map {
                User(
                    id = it[followers[Users.id]],
                    name = it[followers[name]],
                    domain = it[followers[domain]],
                    screenName = it[followers[screenName]],
                    description = it[followers[description]],
                    password = it[followers[password]],
                    inbox = it[followers[inbox]],
                    outbox = it[followers[outbox]],
                    url = it[followers[url]],
                    publicKey = it[followers[publicKey]],
                    privateKey = it[followers[privateKey]],
                    createdAt = Instant.ofEpochMilli(it[followers[createdAt]])
                )
            }
    }

    override suspend fun delete(id: Long) {
        Users.deleteWhere { Users.id.eq(id) }
    }

    override suspend fun deleteFollower(id: Long, follower: Long) {
        UsersFollowers.deleteWhere { (userId eq id).and(followerId eq follower) }
    }

    override suspend fun findAll(): List<User> = Users.selectAll().map { it.toUser() }

    override suspend fun findAllByLimitAndByOffset(limit: Int, offset: Long): List<User> =
        Users.selectAll().limit(limit, offset).map { it.toUser() }

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
