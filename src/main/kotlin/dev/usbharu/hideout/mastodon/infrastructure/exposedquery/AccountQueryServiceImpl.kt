package dev.usbharu.hideout.mastodon.infrastructure.exposedquery

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.mastodon.query.AccountQueryService
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class AccountQueryServiceImpl(private val applicationConfig: ApplicationConfig) : AccountQueryService {
    override suspend fun findById(accountId: Long): Account {

        val query = Actors.select { Actors.id eq accountId }

        return query
            .singleOr { FailedToGetResourcesException("accountId: $accountId wad not exist or duplicate", it) }
            .let { toAccount(it) }
    }

    override suspend fun findByIds(accountIds: List<Long>): List<Account> {
        val query = Actors.select { Actors.id inList accountIds }

        return query
            .map { toAccount(it) }
    }

    private fun toAccount(
        resultRow: ResultRow
    ): Account {
        val userUrl = "${applicationConfig.url}/users/${resultRow[Actors.id]}"

        return Account(
            id = resultRow[Actors.id].toString(),
            username = resultRow[Actors.name],
            acct = "${resultRow[Actors.name]}@${resultRow[Actors.domain]}",
            url = resultRow[Actors.url],
            displayName = resultRow[Actors.screenName],
            note = resultRow[Actors.description],
            avatar = userUrl + "/icon.jpg",
            avatarStatic = userUrl + "/icon.jpg",
            header = userUrl + "/header.jpg",
            headerStatic = userUrl + "/header.jpg",
            locked = resultRow[Actors.locked],
            fields = emptyList(),
            emojis = emptyList(),
            bot = false,
            group = false,
            discoverable = true,
            createdAt = Instant.ofEpochMilli(resultRow[Actors.createdAt]).toString(),
            lastStatusAt = resultRow[Actors.lastPostAt]?.toString(),
            statusesCount = resultRow[Actors.postsCount],
            followersCount = resultRow[Actors.followersCount],
            followingCount = resultRow[Actors.followingCount],
        )
    }
}
