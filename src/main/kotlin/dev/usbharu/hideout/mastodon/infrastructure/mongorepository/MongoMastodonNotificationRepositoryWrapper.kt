package dev.usbharu.hideout.mastodon.infrastructure.mongorepository

import dev.usbharu.hideout.mastodon.domain.model.MastodonNotification
import dev.usbharu.hideout.mastodon.domain.model.MastodonNotificationRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
@ConditionalOnProperty("hideout.use-mongodb", havingValue = "true", matchIfMissing = false)
class MongoMastodonNotificationRepositoryWrapper(private val mongoMastodonNotificationRepository: MongoMastodonNotificationRepository) :
    MastodonNotificationRepository {
    override suspend fun save(mastodonNotification: MastodonNotification): MastodonNotification {
        return mongoMastodonNotificationRepository.save(mastodonNotification)
    }

    override suspend fun deleteById(id: Long) {
        mongoMastodonNotificationRepository.deleteById(id)
    }

    override suspend fun findById(id: Long): MastodonNotification? {
        return mongoMastodonNotificationRepository.findById(id).getOrNull()
    }
}
