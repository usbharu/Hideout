package dev.usbharu.hideout.core.service.block

/**
 * ブロックに関する処理を行います
 *
 */
interface BlockService {
    /**
     * ブロックします
     * 実装はリモートユーザーへのブロックの場合ブロックアクティビティを配送するべきです。
     *
     * @param userId ブロックの動作を行ったユーザーid
     * @param target ブロック対象のユーザーid
     */
    suspend fun block(userId: Long, target: Long)

    /**
     * ブロックを解除します
     * 実装はリモートユーザーへのブロック解除の場合Undo Blockアクティビティを配送するべきです
     *
     * @param userId ブロック解除の動作を行ったユーザーid
     * @param target ブロック解除の対象のユーザーid
     */
    suspend fun unblock(userId: Long, target: Long)
}
