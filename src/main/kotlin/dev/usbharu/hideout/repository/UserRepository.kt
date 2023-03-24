package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.domain.model.UserEntity
import dev.usbharu.hideout.domain.model.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository(private val database: Database) : IUserRepository {
    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    private fun ResultRow.toUser(): User {
        return User(
            this[Users.name],
            this[Users.screenName],
            this[Users.description]
        )
    }

    private fun ResultRow.toUserEntity(): UserEntity {
        return UserEntity(
            this[Users.id].value,
            this[Users.name],
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
                it[screenName] = user.screenName
                it[description] = user.description
            }[Users.id].value, user)
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


    override suspend fun update(userEntity: UserEntity) {
        return query {
            Users.update({ Users.id eq userEntity.id }) {
                it[name] = userEntity.name
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
