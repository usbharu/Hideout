package dev.usbharu.hideout.mastodon.infrastructure.mongorepository

import dev.usbharu.hideout.mastodon.domain.model.MastodonNotification
import org.springframework.data.mongodb.repository.MongoRepository

interface MongoMastodonNotificationRepository : MongoRepository<MastodonNotification, Long> {

    fun deleteByUserId(userId: Long): Long

    fun deleteByIdAndUserId(id: Long, userId: Long): Long
}
