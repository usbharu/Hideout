package dev.usbharu.hideout.mastodon.infrastructure.mongorepository

import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.mastodon.domain.model.MastodonNotification
import dev.usbharu.hideout.mastodon.domain.model.MastodonNotificationRepository
import dev.usbharu.hideout.mastodon.domain.model.NotificationType
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
@ConditionalOnProperty("hideout.use-mongodb", havingValue = "true", matchIfMissing = false)
class MongoMastodonNotificationRepositoryWrapper(
    private val mongoMastodonNotificationRepository: MongoMastodonNotificationRepository,
    private val mongoTemplate: MongoTemplate
) :
    MastodonNotificationRepository {
    override suspend fun save(mastodonNotification: MastodonNotification): MastodonNotification =
        mongoMastodonNotificationRepository.save(mastodonNotification)

    override suspend fun deleteById(id: Long) = mongoMastodonNotificationRepository.deleteById(id)

    override suspend fun findById(id: Long): MastodonNotification? =
        mongoMastodonNotificationRepository.findById(id).getOrNull()

    override suspend fun findByUserIdAndInTypesAndInSourceActorId(
        loginUser: Long,
        types: List<NotificationType>,
        accountId: List<Long>,
        page: Page
    ): PaginationList<MastodonNotification, Long> {
        val query = Query()

        if (page.minId != null) {
            page.minId?.let { query.addCriteria(Criteria.where("id").gt(it)) }
            page.maxId?.let { query.addCriteria(Criteria.where("id").lt(it)) }
        } else {
            query.with(Sort.by(Sort.Direction.DESC, "createdAt"))
            page.sinceId?.let { query.addCriteria(Criteria.where("id").gt(it)) }
            page.maxId?.let { query.addCriteria(Criteria.where("id").lt(it)) }
        }

        page.limit?.let { query.limit(it) }

        val mastodonNotifications = mongoTemplate.find(query, MastodonNotification::class.java)
        return PaginationList(
            mastodonNotifications,
            mastodonNotifications.lastOrNull()?.id,
            mastodonNotifications.firstOrNull()?.id
        )
    }

    override suspend fun deleteByUserId(userId: Long) {
        mongoMastodonNotificationRepository.deleteByUserId(userId)
    }

    override suspend fun deleteByUserIdAndId(userId: Long, id: Long) {
        mongoMastodonNotificationRepository.deleteByIdAndUserId(id, userId)
    }
}
