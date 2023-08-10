package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.domain.model.Acct
import dev.usbharu.hideout.domain.model.hideout.dto.UserResponse

interface IUserApiService {
    suspend fun findAll(limit: Int? = 100, offset: Long = 0): List<UserResponse>

    suspend fun findById(id: Long): UserResponse

    suspend fun findByIds(ids: List<Long>): List<UserResponse>

    suspend fun findByAcct(acct: Acct): UserResponse

    suspend fun findFollowers(userId: Long): List<UserResponse>

    suspend fun findFollowings(userId: Long): List<UserResponse>

    suspend fun findFollowersByAcct(acct: Acct): List<UserResponse>

    suspend fun findFollowingsByAcct(acct: Acct): List<UserResponse>
}
