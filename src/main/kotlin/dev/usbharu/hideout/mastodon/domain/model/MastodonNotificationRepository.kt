package dev.usbharu.hideout.mastodon.domain.model

interface MastodonNotificationRepository {
    suspend fun save(mastodonNotification: MastodonNotification): MastodonNotification
    suspend fun deleteById(id: Long)
    suspend fun findById(id: Long): MastodonNotification?

    @Suppress("LongParameterList", "FunctionMaxLength")
    suspend fun findByUserIdAndMaxIdAndMinIdAndSinceIdAndInTypesAndInSourceActorId(
        loginUser: Long,
        maxId: Long?,
        minId: Long?,
        sinceId: Long?,
        limit: Int,
        typesTmp: MutableList<NotificationType>,
        accountId: List<Long>
    ): List<MastodonNotification>

    suspend fun deleteByUserId(userId: Long)
    suspend fun deleteByUserIdAndId(userId: Long, id: Long)
}
