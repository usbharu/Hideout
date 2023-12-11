package dev.usbharu.hideout.core.domain.model.userdetails

interface UserDetailRepository {
    suspend fun save(userDetail: UserDetail): UserDetail
    suspend fun delete(userDetail: UserDetail)
    suspend fun findByActorId(actorId: Long): UserDetail?
}
