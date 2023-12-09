package dev.usbharu.hideout.core.domain.model.relationship

/**
 * [Relationship]の永続化
 *
 */
interface RelationshipRepository {
    /**
     * 永続化します
     *
     * @param relationship 永続化する[Relationship]
     * @return 永続化された[Relationship]
     */
    suspend fun save(relationship: Relationship): Relationship

    /**
     * 永続化されたものを削除します
     *
     * @param relationship 削除する[Relationship]
     */
    suspend fun delete(relationship: Relationship)

    /**
     * userIdとtargetUserIdで[Relationship]を取得します
     *
     * @param userId 取得するユーザーID
     * @param targetUserId 対象ユーザーID
     * @return 取得された[Relationship] 存在しない場合nullが返ります
     */
    suspend fun findByUserIdAndTargetUserId(userId: Long, targetUserId: Long): Relationship?
}
