package dev.usbharu.hideout.mastodon.infrastructure.mongorepository

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

    override suspend fun findByUserIdAndMaxIdAndMinIdAndSinceIdAndInTypesAndInSourceActorId(
        loginUser: Long,
        maxId: Long?,
        minId: Long?,
        sinceId: Long?,
        limit: Int,
        typesTmp: MutableList<NotificationType>,
        accountId: List<Long>
    ): List<MastodonNotification> {
        val query = Query()

        if (maxId != null) {
            val criteria = Criteria.where("id").lte(maxId)
            query.addCriteria(criteria)
        }

        if (minId != null) {
            val criteria = Criteria.where("id").gte(minId)
            query.addCriteria(criteria)
        }
        if (sinceId != null) {
            val criteria = Criteria.where("id").gte(sinceId)
            query.addCriteria(criteria)
        }

        query.limit(limit)
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"))

        return mongoTemplate.find(query, MastodonNotification::class.java)
    }

    override suspend fun deleteByUserId(userId: Long) {
        mongoMastodonNotificationRepository.deleteByUserId(userId)
    }

    override suspend fun deleteByUserIdAndId(userId: Long, id: Long) {
        mongoMastodonNotificationRepository.deleteByIdAndUserId(id, userId)
    }
}
