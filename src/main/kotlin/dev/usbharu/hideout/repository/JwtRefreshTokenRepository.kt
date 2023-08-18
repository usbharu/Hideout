package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.JwtRefreshToken
import org.springframework.stereotype.Repository

@Repository
interface JwtRefreshTokenRepository {
    suspend fun generateId(): Long

    suspend fun save(token: JwtRefreshToken)

    suspend fun findById(id: Long): JwtRefreshToken?

    suspend fun delete(token: JwtRefreshToken)
}