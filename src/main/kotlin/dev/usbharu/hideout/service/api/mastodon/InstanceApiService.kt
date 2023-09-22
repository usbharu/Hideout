package dev.usbharu.hideout.service.api.mastodon

import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.domain.mastodon.model.generated.*
import org.springframework.stereotype.Service
import java.net.URL

@Service
interface InstanceApiService {
    suspend fun v1Instance(): V1Instance
}

@Service
class InstanceApiServiceImpl(private val applicationConfig: ApplicationConfig) : InstanceApiService {
    override suspend fun v1Instance(): V1Instance {
        val url = applicationConfig.url
        val url1 = URL(url)
        return V1Instance(
            uri = url1.host,
            title = "Hideout Server",
            shortDescription = "Hideout test server",
            description = "This server is operated for testing of Hideout. We are not responsible for any events that occur when associating with this server",
            email = "i@usbharu.dev",
            version = "0.0.1",
            urls = V1InstanceUrls("wss://${url1.host}"),
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
                    listOf(),
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