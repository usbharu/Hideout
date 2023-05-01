package dev.usbharu.hideout.domain.model.hideout.entity

import java.time.Instant

data class JwtRefreshToken(
    val id: Long,
    val userId: Long,
    val refreshToken: String,
    val createdAt: Instant,
    val expiresAt: Instant
)
