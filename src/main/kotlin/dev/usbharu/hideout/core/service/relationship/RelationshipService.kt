package dev.usbharu.hideout.core.service.relationship

interface RelationshipService {
    suspend fun followRequest(userId: Long, targetId: Long)
    suspend fun block(userId: Long, targetId: Long)
    suspend fun acceptFollowRequest(userId: Long, targetId: Long)
    suspend fun rejectFollowRequest(userId: Long, targetId: Long)
    suspend fun ignoreFollowRequest(userId: Long, targetId: Long)
    suspend fun unfollow(userId: Long, targetId: Long)
    suspend fun unblock(userId: Long, targetId: Long)
    suspend fun mute(userId: Long, targetId: Long)
    suspend fun unmute(userId: Long, targetId: Long)

}
