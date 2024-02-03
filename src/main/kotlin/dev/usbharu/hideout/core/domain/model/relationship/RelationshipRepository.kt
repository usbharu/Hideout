package dev.usbharu.hideout.core.domain.model.relationship

import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList

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
     * @param actorId 取得するユーザーID
     * @param targetActorId 対象ユーザーID
     * @return 取得された[Relationship] 存在しない場合nullが返ります
     */
    suspend fun findByUserIdAndTargetUserId(actorId: Long, targetActorId: Long): Relationship?

    suspend fun deleteByActorIdOrTargetActorId(actorId: Long, targetActorId: Long)

    suspend fun findByTargetIdAndFollowing(targetId: Long, following: Boolean): List<Relationship>

    @Suppress("FunctionMaxLength")
    suspend fun findByTargetIdAndFollowRequestAndIgnoreFollowRequest(
        targetId: Long,
        followRequest: Boolean,
        ignoreFollowRequest: Boolean,
        page: Page.PageByMaxId
    ): PaginationList<Relationship, Long>

    suspend fun findByActorIdAndMuting(
        actorId: Long,
        muting: Boolean,
        page: Page.PageByMaxId
    ): PaginationList<Relationship, Long>
}
