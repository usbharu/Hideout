package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.JwtRefreshToken

interface IJwtRefreshTokenRepository {
    suspend fun generateId():Long

    suspend fun save(token: JwtRefreshToken)

    suspend fun findById(id:Long):JwtRefreshToken?
    suspend fun findByToken(token:String):JwtRefreshToken?
    suspend fun findByUserId(userId:Long):JwtRefreshToken?

    suspend fun delete(token:JwtRefreshToken)
    suspend fun deleteById(id:Long)
    suspend fun deleteByToken(token:String)
    suspend fun deleteByUserId(userId:Long)

    suspend fun deleteAll()
}
