package dev.usbharu.hideout.mastodon.domain.model

interface MastodonNotificationRepository {
    suspend fun save(mastodonNotification: MastodonNotification): MastodonNotification
    suspend fun deleteById(id: Long)
    suspend fun findById(id: Long): MastodonNotification?
}
