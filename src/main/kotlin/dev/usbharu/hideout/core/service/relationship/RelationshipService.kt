package dev.usbharu.hideout.core.service.relationship

interface RelationshipService {
    suspend fun followRequest(userId: Long, targetId: Long)
    suspend fun block(userId: Long, targetId: Long)

    /**
     * フォローリクエストを承認します
     * [userId]が[targetId]からのフォローリクエストを承認します
     *
     * @param userId 承認操作をするユーザー
     * @param targetId 承認するフォローリクエストを送ってきたユーザー
     * @param force 強制的にAcceptアクティビティを発行する
     */
    suspend fun acceptFollowRequest(userId: Long, targetId: Long, force: Boolean = false)
    suspend fun rejectFollowRequest(userId: Long, targetId: Long)
    suspend fun ignoreFollowRequest(userId: Long, targetId: Long)
    suspend fun unfollow(userId: Long, targetId: Long)
    suspend fun unblock(userId: Long, targetId: Long)
    suspend fun mute(userId: Long, targetId: Long)
    suspend fun unmute(userId: Long, targetId: Long)
}
