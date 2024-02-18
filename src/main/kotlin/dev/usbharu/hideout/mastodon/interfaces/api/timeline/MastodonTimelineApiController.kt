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

package dev.usbharu.hideout.mastodon.interfaces.api.timeline

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.toHttpHeader
import dev.usbharu.hideout.controller.mastodon.generated.TimelineApi
import dev.usbharu.hideout.core.infrastructure.springframework.security.LoginUserContextHolder
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.service.timeline.TimelineApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class MastodonTimelineApiController(
    private val timelineApiService: TimelineApiService,
    private val loginUserContextHolder: LoginUserContextHolder,
    private val applicationConfig: ApplicationConfig,
) : TimelineApi {
    override fun apiV1TimelinesHomeGet(
        maxId: String?,
        sinceId: String?,
        minId: String?,
        limit: Int?
    ): ResponseEntity<Flow<Status>> = runBlocking {
        val homeTimeline = timelineApiService.homeTimeline(
            userId = loginUserContextHolder.getLoginUserId(),
            page = Page.of(
                maxId = maxId?.toLongOrNull(),
                minId = minId?.toLongOrNull(),
                sinceId = sinceId?.toLongOrNull(),
                limit = limit?.coerceIn(0, 80) ?: 40
            )
        )

        val httpHeader = homeTimeline.toHttpHeader(
            { "${applicationConfig.url}/api/v1/home?max_id=$it" },
            { "${applicationConfig.url}/api/v1/home?min_id=$it" }
        ) ?: return@runBlocking ResponseEntity(
            homeTimeline.asFlow(),
            HttpStatus.OK
        )
        ResponseEntity.ok().header("Link", httpHeader).body(homeTimeline.asFlow())
    }

    override fun apiV1TimelinesPublicGet(
        local: Boolean?,
        remote: Boolean?,
        onlyMedia: Boolean?,
        maxId: String?,
        sinceId: String?,
        minId: String?,
        limit: Int?
    ): ResponseEntity<Flow<Status>> = runBlocking {
        val publicTimeline = timelineApiService.publicTimeline(
            localOnly = local ?: false,
            remoteOnly = remote ?: false,
            mediaOnly = onlyMedia ?: false,
            page = Page.of(
                maxId = maxId?.toLongOrNull(),
                minId = minId?.toLongOrNull(),
                sinceId = sinceId?.toLongOrNull(),
                limit = limit?.coerceIn(0, 80) ?: 40
            )
        )

        val httpHeader = publicTimeline.toHttpHeader(
            { "${applicationConfig.url}/api/v1/public?max_id=$it" },
            { "${applicationConfig.url}/api/v1/public?min_id=$it" }
        ) ?: return@runBlocking ResponseEntity(
            publicTimeline.asFlow(),
            HttpStatus.OK
        )
        ResponseEntity.ok().header("Link", httpHeader).body(publicTimeline.asFlow())
    }
}
