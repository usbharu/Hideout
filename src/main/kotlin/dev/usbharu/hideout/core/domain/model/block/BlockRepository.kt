package dev.usbharu.hideout.core.domain.model.block

/**
 * ブロックの状態を永続化します
 *
 */
interface BlockRepository {
    /**
     * ブロックの状態を永続化します
     *
     * @param block 永続化するブロック
     * @return 永続化されたブロック
     */
    suspend fun save(block: Block): Block

    /**
     * ブロックの状態を削除します
     *
     * @param block 削除する永続化されたブロック
     */
    suspend fun delete(block: Block)

    /**
     *
     *
     * @param userId
     * @param target
     * @return
     */
    suspend fun findByUserIdAndTarget(userId: Long, target: Long): Block
}
