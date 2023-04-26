package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository(private val database: Database) : IUserRepository {
    init {
        transaction(database) {
            SchemaUtils.create(Users)
            SchemaUtils.create(UsersFollowers)
            SchemaUtils.createMissingTablesAndColumns(Users)
            SchemaUtils.createMissingTablesAndColumns(UsersFollowers)
        }
    }

    private fun ResultRow.toUserEntity(): UserEntity {
        return UserEntity(
            this[Users.id],
            this[Users.name],
            this[Users.domain],
            this[Users.screenName],
            this[Users.description],
            this[Users.inbox],
            this[Users.outbox],
            this[Users.url],
        )
    }

    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun create(user: User): UserEntity {
        return query {
            UserEntity(Users.insert {
                it[name] = user.name
                it[domain] = user.domain
                it[screenName] = user.screenName
                it[description] = user.description
                it[inbox] = user.inbox
                it[outbox] = user.outbox
                it[url] = user.url
            }[Users.id], user)
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

    override suspend fun findById(id: Long): UserEntity? {
        return query {
            Users.select { Users.id eq id }.map {
                it.toUserEntity()
            }.singleOrNull()
        }
    }

    override suspend fun findByIds(ids: List<Long>): List<UserEntity> {
        return query {
            Users.select { Users.id inList ids }.map {
                it.toUserEntity()
            }
        }
    }

    override suspend fun findByName(name: String): UserEntity? {
        return query {
            Users.select { Users.name eq name }.map {
                it.toUserEntity()
            }.singleOrNull()
        }
    }

    override suspend fun findByNameAndDomains(names: List<Pair<String, String>>): List<UserEntity> {
        return query {
            val selectAll = Users.selectAll()
            names.forEach { (name, domain) ->
                selectAll.orWhere { Users.name eq name and (Users.domain eq domain) }
            }
            selectAll.map { it.toUserEntity() }
        }
    }

    override suspend fun findByUrl(url: String): UserEntity? {
        return query {
            Users.select { Users.url eq url }.singleOrNull()?.toUserEntity()
        }
    }

    override suspend fun findByUrls(urls: List<String>): List<UserEntity> {
        return query {
            Users.select { Users.url inList urls }.map { it.toUserEntity() }
        }
    }

    override suspend fun findFollowersById(id: Long): List<UserEntity> {
        return query {
            val followers = Users.alias("FOLLOWERS")
            Users.innerJoin(
                otherTable = UsersFollowers,
                onColumn = { Users.id },
                otherColumn = { UsersFollowers.userId })

                .innerJoin(
                    otherTable = followers,
                    onColumn = { UsersFollowers.followerId },
                    otherColumn = { followers[Users.id] })

                .slice(
                    followers.get(Users.id),
                    followers.get(Users.name),
                    followers.get(Users.domain),
                    followers.get(Users.screenName),
                    followers.get(Users.description),
                    followers.get(Users.inbox),
                    followers.get(Users.outbox),
                    followers.get(Users.url)
                )
                .select { Users.id eq id }
                .map {
                    UserEntity(
                        id = it[followers[Users.id]],
                        name = it[followers[Users.name]],
                        domain = it[followers[Users.domain]],
                        screenName = it[followers[Users.screenName]],
                        description = it[followers[Users.description]],
                        inbox = it[followers[Users.inbox]],
                        outbox = it[followers[Users.outbox]],
                        url = it[followers[Users.url]],
                    )
                }
        }
    }


    override suspend fun update(userEntity: UserEntity) {
        return query {
            Users.update({ Users.id eq userEntity.id }) {
                it[name] = userEntity.name
                it[domain] = userEntity.domain
                it[screenName] = userEntity.screenName
                it[description] = userEntity.description
                it[inbox] = userEntity.inbox
                it[outbox] = userEntity.outbox
                it[url] = userEntity.url
            }
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

    override suspend fun findAllByLimitAndByOffset(limit: Int, offset: Long): List<UserEntity> {
        return query {
            Users.selectAll().limit(limit, offset).map { it.toUserEntity() }
        }
    }
}
