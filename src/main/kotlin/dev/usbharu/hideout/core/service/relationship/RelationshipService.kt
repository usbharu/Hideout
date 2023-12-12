package dev.usbharu.hideout.core.service.relationship

interface RelationshipService {
    suspend fun followRequest(actorId: Long, targetId: Long)
    suspend fun block(actorId: Long, targetId: Long)

    /**
     * フォローリクエストを承認します
     * [actorId]が[targetId]からのフォローリクエストを承認します
     *
     * @param actorId 承認操作をするユーザー
     * @param targetId 承認するフォローリクエストを送ってきたユーザー
     * @param force 強制的にAcceptアクティビティを発行する
     */
    suspend fun acceptFollowRequest(actorId: Long, targetId: Long, force: Boolean = false)
    suspend fun rejectFollowRequest(actorId: Long, targetId: Long)
    suspend fun ignoreFollowRequest(actorId: Long, targetId: Long)
    suspend fun unfollow(actorId: Long, targetId: Long)
    suspend fun unblock(actorId: Long, targetId: Long)
    suspend fun mute(actorId: Long, targetId: Long)
    suspend fun unmute(actorId: Long, targetId: Long)
}
