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

package dev.usbharu.hideout.core.service.timeline

import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.application.infrastructure.exposed.withPagination
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Timelines
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.interfaces.api.status.StatusQuery
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("hideout.use-mongodb", havingValue = "false", matchIfMissing = true)
class ExposedGenerateTimelineService(private val statusQueryService: StatusQueryService) : GenerateTimelineService {

    override suspend fun getTimeline(
        forUserId: Long?,
        localOnly: Boolean,
        mediaOnly: Boolean,
        page: Page
    ): PaginationList<Status, Long> {
        val query = Timelines.selectAll()

        if (forUserId != null) {
            query.andWhere { Timelines.userId eq forUserId }
        }
        if (localOnly) {
            query.andWhere { Timelines.isLocal eq true }
        }
        val result = query.withPagination(page, Timelines.id)

        val statusQueries = result.map {
            StatusQuery(
                it[Timelines.postId],
                it[Timelines.replyId],
                it[Timelines.repostId],
                it[Timelines.mediaIds].split(",").mapNotNull { s -> s.toLongOrNull() },
                it[Timelines.emojiIds].split(",").mapNotNull { s -> s.toLongOrNull() }
            )
        }

        val findByPostIdsWithMediaIds = statusQueryService.findByPostIdsWithMediaIds(statusQueries)
        return PaginationList(
            findByPostIdsWithMediaIds,
            findByPostIdsWithMediaIds.lastOrNull()?.id?.toLongOrNull(),
            findByPostIdsWithMediaIds.firstOrNull()?.id?.toLongOrNull()
        )
    }
}
