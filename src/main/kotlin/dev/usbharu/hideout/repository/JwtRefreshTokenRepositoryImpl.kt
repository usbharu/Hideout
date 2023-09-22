package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.JwtRefreshToken
import dev.usbharu.hideout.service.core.IdGenerateService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JwtRefreshTokenRepositoryImpl(
    private val database: Database,
    private val idGenerateService: IdGenerateService
) :
    JwtRefreshTokenRepository {

    init {
        transaction(database) {
            SchemaUtils.create(JwtRefreshTokens)
            SchemaUtils.createMissingTablesAndColumns(JwtRefreshTokens)
        }
    }

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(token: JwtRefreshToken) {
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

    override suspend fun findById(id: Long): JwtRefreshToken? =
        JwtRefreshTokens.select { JwtRefreshTokens.id.eq(id) }.singleOrNull()?.toJwtRefreshToken()

    override suspend fun delete(token: JwtRefreshToken) {
        JwtRefreshTokens.deleteWhere { id eq token.id }
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
