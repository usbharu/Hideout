package dev.usbharu.hideout.mastodon.query

import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.interfaces.api.status.StatusQuery

interface StatusQueryService {
    suspend fun findByPostIds(ids: List<Long>): List<Status>
    suspend fun findByPostIdsWithMediaIds(statusQueries: List<StatusQuery>): List<Status>

    /**
     * アカウントの投稿一覧を取得します
     *
     * @param accountId 対象アカウントのid
     * @param maxId 投稿の最大id
     * @param sinceId 投稿の最小id
     * @param minId 不明
     * @param limit 投稿の最大件数
     * @param onlyMedia メディア付き投稿のみ
     * @param excludeReplies 返信を除外
     * @param excludeReblogs リブログを除外
     * @param pinned ピン止め投稿のみ
     * @param tagged タグ付き?
     * @param includeFollowers フォロワー限定投稿を含める
     */
    @Suppress("LongParameterList")
    suspend fun accountsStatus(
        accountId: Long,
        maxId: Long? = null,
        sinceId: Long? = null,
        minId: Long? = null,
        limit: Int,
        onlyMedia: Boolean = false,
        excludeReplies: Boolean = false,
        excludeReblogs: Boolean = false,
        pinned: Boolean = false,
        tagged: String? = null,
        includeFollowers: Boolean = false
    ): List<Status>
}
