package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.JwtRefreshToken
import dev.usbharu.hideout.repository.JwtRefreshTokens
import dev.usbharu.hideout.repository.toJwtRefreshToken
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single

@Single
class JwtRefreshTokenQueryServiceImpl : JwtRefreshTokenQueryService {
    override suspend fun findById(id: Long): JwtRefreshToken =
        JwtRefreshTokens.select { JwtRefreshTokens.id.eq(id) }.single().toJwtRefreshToken()

    override suspend fun findByToken(token: String): JwtRefreshToken =
        JwtRefreshTokens.select { JwtRefreshTokens.refreshToken.eq(token) }.single().toJwtRefreshToken()

    override suspend fun findByUserId(userId: Long): JwtRefreshToken =
        JwtRefreshTokens.select { JwtRefreshTokens.userId.eq(userId) }.single().toJwtRefreshToken()

    override suspend fun deleteById(id: Long) {
        JwtRefreshTokens.deleteWhere { JwtRefreshTokens.id eq id }
    }

    override suspend fun deleteByToken(token: String) {
        JwtRefreshTokens.deleteWhere { refreshToken eq token }
    }

    override suspend fun deleteByUserId(userId: Long) {
        JwtRefreshTokens.deleteWhere { JwtRefreshTokens.userId eq userId }
    }

    override suspend fun deleteAll() {
        JwtRefreshTokens.deleteAll()
    }
}
