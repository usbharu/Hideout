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

        page.limit?.let { query.limit(it) }

        val mastodonNotifications = if (page.minId != null) {
            query.with(Sort.by(Sort.Direction.ASC, "id"))
            page.minId?.let { query.addCriteria(Criteria.where("id").gt(it)) }
            page.maxId?.let { query.addCriteria(Criteria.where("id").lt(it)) }
            mongoTemplate.find(query, MastodonNotification::class.java).reversed()
        } else {
            query.with(Sort.by(Sort.Direction.DESC, "id"))
            page.sinceId?.let { query.addCriteria(Criteria.where("id").gt(it)) }
            page.maxId?.let { query.addCriteria(Criteria.where("id").lt(it)) }
            mongoTemplate.find(query, MastodonNotification::class.java)
        }

        return PaginationList(
            mastodonNotifications,
            mastodonNotifications.firstOrNull()?.id,
            mastodonNotifications.lastOrNull()?.id
        )
    }

    override suspend fun deleteByUserId(userId: Long) {
        mongoMastodonNotificationRepository.deleteByUserId(userId)
    }

    override suspend fun deleteByUserIdAndId(userId: Long, id: Long) {
        mongoMastodonNotificationRepository.deleteByIdAndUserId(id, userId)
    }
}
