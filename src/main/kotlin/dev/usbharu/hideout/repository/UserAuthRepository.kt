package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.UserAuthentication
import dev.usbharu.hideout.domain.model.UserAuthenticationEntity
import dev.usbharu.hideout.domain.model.UsersAuthentication
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class UserAuthRepository(private val database: Database) : IUserAuthRepository {

    init {
        transaction(database) {
            SchemaUtils.create(UsersAuthentication)
            SchemaUtils.createMissingTablesAndColumns(UsersAuthentication)
        }
    }

    private fun ResultRow.toUserAuth():UserAuthenticationEntity{
        return UserAuthenticationEntity(
            id = this[UsersAuthentication.id].value,
            userId = this[UsersAuthentication.userId],
            hash = this[UsersAuthentication.hash],
            publicKey = this[UsersAuthentication.publicKey],
            privateKey = this[UsersAuthentication.privateKey]
        )
    }


    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {block()}
    override suspend fun create(userAuthentication: UserAuthentication): UserAuthenticationEntity {
        return query {
            UserAuthenticationEntity(
                UsersAuthentication.insert {
                    it[userId] = userAuthentication.userId
                    it[hash] = userAuthentication.hash
                    it[publicKey] = userAuthentication.publicKey
                    it[privateKey] = userAuthentication.privateKey
                }[UsersAuthentication.id].value,userAuthentication
            )
        }
    }

    override suspend fun findById(id: Long): UserAuthenticationEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun findByUserId(id:Long):UserAuthenticationEntity? {
        return query {
            UsersAuthentication.select { UsersAuthentication.userId eq id }.map { it.toUserAuth() }.singleOrNull()
        }
    }

    override suspend fun update(userAuthenticationEntity: UserAuthenticationEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Long) {
        TODO("Not yet implemented")
    }
}
