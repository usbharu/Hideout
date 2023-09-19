package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.JwtRefreshToken
import org.springframework.stereotype.Repository

@Repository
interface JwtRefreshTokenQueryService {
    suspend fun findById(id: Long): JwtRefreshToken
    suspend fun findByToken(token: String): JwtRefreshToken
    suspend fun findByUserId(userId: Long): JwtRefreshToken
    suspend fun deleteById(id: Long)
    suspend fun deleteByToken(token: String)
    suspend fun deleteByUserId(userId: Long)
    suspend fun deleteAll()
}
