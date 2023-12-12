package dev.usbharu.hideout.mastodon.service.account

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import org.springframework.stereotype.Service

@Service
interface AccountService {
    suspend fun findById(id: Long): Account
    suspend fun findByIds(ids: List<Long>): List<Account>
}

@Service
class AccountServiceImpl(
    private val actorQueryService: ActorQueryService,
    private val applicationConfig: ApplicationConfig
) : AccountService {
    override suspend fun findById(id: Long): Account {
        val findById = actorQueryService.findById(id)
        return toAccount(findById)
    }

    private fun toAccount(findById: Actor): Account {
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

    override suspend fun findByIds(ids: List<Long>): List<Account> =
        actorQueryService.findByIds(ids).map { toAccount(it) }
}
