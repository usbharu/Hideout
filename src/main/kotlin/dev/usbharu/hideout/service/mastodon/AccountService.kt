package dev.usbharu.hideout.service.mastodon

import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.query.UserQueryService
import org.springframework.stereotype.Service

@Service
interface AccountService {
    suspend fun findById(id: Long): Account
}

@Service
class AccountServiceImpl(private val userQueryService: UserQueryService) : AccountService {
    override suspend fun findById(id: Long): Account {
        val findById = userQueryService.findById(id)
        return Account(
            id = findById.id.toString(),
            username = findById.name,
            acct = "${findById.name}@${findById.domain}",
            url = findById.url,
            displayName = findById.screenName,
            note = findById.description,
            avatar = findById.url + "/icon.jpg",
            avatarStatic = findById.url + "/icon.jpg",
            header = findById.url + "/header.jpg",
            headerStatic = findById.url + "/header.jpg",
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
