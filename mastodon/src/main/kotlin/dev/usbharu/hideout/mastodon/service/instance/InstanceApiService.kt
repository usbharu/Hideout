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

package dev.usbharu.hideout.mastodon.service.instance

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.domain.mastodon.model.generated.*
import org.springframework.stereotype.Service

@Service
interface InstanceApiService {
    suspend fun v1Instance(): V1Instance
}

@Service
class InstanceApiServiceImpl(private val applicationConfig: ApplicationConfig) : InstanceApiService {
    @Suppress("LongMethod")
    override suspend fun v1Instance(): V1Instance {
        val url = applicationConfig.url
        return V1Instance(
            uri = url.host,
            title = "Hideout Server",
            shortDescription = "Hideout test server",
            description = "This server is operated for testing of Hideout." +
                " We are not responsible for any events that occur when associating with this server",
            email = "i@usbharu.dev",
            version = "0.0.1",
            urls = V1InstanceUrls("wss://${url.host}"),
            stats = V1InstanceStats(1, 0, 0),
            thumbnail = null,
            languages = listOf("ja-JP"),
            registrations = false,
            approvalRequired = false,
            invitesEnabled = false,
            configuration = V1InstanceConfiguration(
                accounts = V1InstanceConfigurationAccounts(1),
                statuses = V1InstanceConfigurationStatuses(
                    300,
                    4,
                    23
                ),
                mediaAttachments = V1InstanceConfigurationMediaAttachments(
                    emptyList(),
                    0,
                    0,
                    0,
                    0
                ),
                polls = V1InstanceConfigurationPolls(
                    0,
                    0,
                    0,
                    0
                )
            ),
            contactAccount = Account(
                id = "0",
                username = "",
                acct = "",
                url = "",
                displayName = "",
                note = "",
                avatar = "",
                avatarStatic = "",
                header = "",
                headerStatic = "",
                locked = false,
                fields = emptyList(),
                emojis = emptyList(),
                bot = false,
                group = false,
                discoverable = false,
                createdAt = "0",
                lastStatusAt = "0",
                statusesCount = 1,
                followersCount = 0,
                noindex = false,
                moved = false,
                suspendex = false,
                limited = false,
                followingCount = 0
            ),
            rules = emptyList()
        )
    }
}
