package dev.usbharu.hideout.mastodon.service.account

import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.mastodon.query.AccountQueryService
import org.springframework.stereotype.Service

@Service
interface AccountService {
    suspend fun findById(id: Long): Account
    suspend fun findByIds(ids: List<Long>): List<Account>
}

@Service
class AccountServiceImpl(
    private val accountQueryService: AccountQueryService
) : AccountService {
    override suspend fun findById(id: Long): Account = accountQueryService.findById(id)

    override suspend fun findByIds(ids: List<Long>): List<Account> = accountQueryService.findByIds(ids)
}
