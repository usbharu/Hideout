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

package dev.usbharu.hideout.mastodon.service.timeline

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.core.service.timeline.GenerateTimelineService
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import org.springframework.stereotype.Service

@Suppress("LongParameterList")
interface TimelineApiService {

    suspend fun publicTimeline(
        localOnly: Boolean = false,
        remoteOnly: Boolean = false,
        mediaOnly: Boolean = false,
        page: Page
    ): PaginationList<Status, Long>

    suspend fun homeTimeline(
        userId: Long,
        page: Page
    ): PaginationList<Status, Long>
}

@Service
class TimelineApiServiceImpl(
    private val generateTimelineService: GenerateTimelineService,
    private val transaction: Transaction
) : TimelineApiService {

    override suspend fun publicTimeline(
        localOnly: Boolean,
        remoteOnly: Boolean,
        mediaOnly: Boolean,
        page: Page
    ): PaginationList<Status, Long> = transaction.transaction {
        return@transaction generateTimelineService.getTimeline(forUserId = 0, localOnly, mediaOnly, page)
    }

    override suspend fun homeTimeline(userId: Long, page: Page): PaginationList<Status, Long> =
        transaction.transaction {
            return@transaction generateTimelineService.getTimeline(forUserId = userId, page = page)
        }
}
