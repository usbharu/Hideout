package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.JwtRefreshToken
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class JwtRefreshTokenRepositoryImpl(private val database: Database) : IJwtRefreshTokenRepository {

    init {
        transaction(database){
            SchemaUtils.create(JwtRefreshTokens)
            SchemaUtils.createMissingTablesAndColumns(JwtRefreshTokens)
        }
    }

    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun save(token: JwtRefreshToken) {
        query {
            if (JwtRefreshTokens.select { JwtRefreshTokens.id.eq(token.id) }.empty()) {
                JwtRefreshTokens.insert {
                    it[id] = token.id
                    it[userId] = token.userId
                    it[refreshToken] = token.refreshToken
                    it[createdAt] = token.createdAt.toEpochMilli()
                    it[expiresAt] = token.expiresAt.toEpochMilli()
                }
            } else {
                JwtRefreshTokens.update({ JwtRefreshTokens.id eq token.id }) {
                    it[userId] = token.userId
                    it[refreshToken] = token.refreshToken
                    it[createdAt] = token.createdAt.toEpochMilli()
                    it[expiresAt] = token.expiresAt.toEpochMilli()
                }
            }
        }
    }

    override suspend fun findById(id: Long): JwtRefreshToken? {
        return query {
            JwtRefreshTokens.select { JwtRefreshTokens.id.eq(id) }.singleOrNull()?.toJwtRefreshToken()
        }
    }

    override suspend fun findByToken(token: String): JwtRefreshToken? {
        return query {
            JwtRefreshTokens.select { JwtRefreshTokens.refreshToken.eq(token) }.singleOrNull()?.toJwtRefreshToken()
        }
    }
}

fun ResultRow.toJwtRefreshToken(): JwtRefreshToken {
    return JwtRefreshToken(
        this[JwtRefreshTokens.id],
        this[JwtRefreshTokens.userId],
        this[JwtRefreshTokens.refreshToken],
        Instant.ofEpochMilli(this[JwtRefreshTokens.createdAt]),
        Instant.ofEpochMilli(this[JwtRefreshTokens.expiresAt])
    )
}

object JwtRefreshTokens : Table("jwt_refresh_tokens") {
    val id = long("id")
    val userId = long("user_id")
    val refreshToken = varchar("refresh_token", 1000)
    val createdAt = long("created_at")
    val expiresAt = long("expires_at")
    override val primaryKey = PrimaryKey(id)
}
