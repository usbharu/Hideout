package dev.usbharu.hideout.mastodon.query

import dev.usbharu.hideout.domain.mastodon.model.generated.Account

interface AccountQueryService {
    suspend fun findById(accountId: Long): Account?
    suspend fun findByIds(accountIds: List<Long>): List<Account>
}
