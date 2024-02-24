/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    suspend fun countByTargetIdAndFollowing(targetId: Long, following: Boolean): Int

    suspend fun countByUserIdAndFollowing(userId: Long, following: Boolean): Int

    @Suppress("FunctionMaxLength")
    suspend fun findByTargetIdAndFollowRequestAndIgnoreFollowRequest(
        targetId: Long,
        followRequest: Boolean,
        ignoreFollowRequest: Boolean,
        page: Page.PageByMaxId,
    ): PaginationList<Relationship, Long>

    suspend fun findByActorIdAndMuting(
        actorId: Long,
        muting: Boolean,
        page: Page.PageByMaxId,
    ): PaginationList<Relationship, Long>
}
