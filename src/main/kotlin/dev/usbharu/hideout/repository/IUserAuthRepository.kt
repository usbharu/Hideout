package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.UserAuthentication
import dev.usbharu.hideout.domain.model.UserAuthenticationEntity

interface IUserAuthRepository {
    suspend fun create(userAuthentication: UserAuthentication):UserAuthenticationEntity

    suspend fun findById(id:Long):UserAuthenticationEntity?

    suspend fun update(userAuthenticationEntity: UserAuthenticationEntity)

    suspend fun delete(id:Long)
    suspend fun findByUserId(id: Long): UserAuthenticationEntity?
}
