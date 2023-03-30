package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.domain.model.UserEntity
import dev.usbharu.hideout.domain.model.Users
import dev.usbharu.hideout.domain.model.UsersFollowers
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

    private fun ResultRow.toUser(): User {
        return User(
            this[Users.name],
            this[Users.domain],
            this[Users.screenName],
            this[Users.description]
        )
    }

    private fun ResultRow.toUserEntity(): UserEntity {
        return UserEntity(
            this[Users.id].value,
            this[Users.name],
            this[Users.domain],
            this[Users.screenName],
            this[Users.description]
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
            }[Users.id].value, user)
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

    override suspend fun findByName(name: String): UserEntity? {
        return query {
            Users.select { Users.name eq name }.map {
                it.toUserEntity()
            }.singleOrNull()
        }
    }

    override suspend fun findFollowersById(id: Long): List<UserEntity> {
        return query {
            val followers = Users.alias("followers")
            Users.leftJoin(
                otherTable = UsersFollowers,
                onColumn = { Users.id },
                otherColumn = { UsersFollowers.userId })
                .leftJoin(
                    otherTable = followers,
                    onColumn = { UsersFollowers.followerId },
                    otherColumn = { followers[Users.id] })
                .select { Users.id eq id }
                .map {
                    UserEntity(
                        id = it[followers[Users.id]].value,
                        name = it[followers[Users.name]],
                        domain = it[followers[Users.domain]],
                        screenName = it[followers[Users.screenName]],
                        description = it[followers[Users.description]],
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
