package dev.usbharu.hideout.mastodon.service.account

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import org.springframework.stereotype.Service

@Service
interface AccountService {
    suspend fun findById(id: Long): Account
}

@Service
class AccountServiceImpl(
    private val userQueryService: UserQueryService,
    private val applicationConfig: ApplicationConfig
) : AccountService {
    override suspend fun findById(id: Long): Account {
        val findById = userQueryService.findById(id)
        val userUrl = applicationConfig.url.toString() + "/users/" + findById.id.toString()

        return Account(
            id = findById.id.toString(),
            username = findById.name,
            acct = "${findById.name}@${findById.domain}",
            url = findById.url,
            displayName = findById.screenName,
            note = findById.description,
            avatar = "$userUrl/icon.jpg",
            avatarStatic = "$userUrl/icon.jpg",
            header = "$userUrl/header.jpg",
            headerStatic = "$userUrl/header.jpg",
            locked = false,
            fields = emptyList(),
            emojis = emptyList(),
            bot = false,
            group = false,
            discoverable = false,
            createdAt = findById.createdAt.toString(),
            lastStatusAt = findById.createdAt.toString(),
            statusesCount = 0,
            followersCount = 0,
        )
    }
}
