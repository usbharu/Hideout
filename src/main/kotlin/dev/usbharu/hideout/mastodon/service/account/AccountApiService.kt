package dev.usbharu.hideout.mastodon.service.account

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.service.user.UserCreateDto
import dev.usbharu.hideout.core.service.user.UserService
import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.domain.mastodon.model.generated.CredentialAccount
import dev.usbharu.hideout.domain.mastodon.model.generated.CredentialAccountSource
import dev.usbharu.hideout.domain.mastodon.model.generated.Role
import org.springframework.stereotype.Service

@Service
interface AccountApiService {
    suspend fun verifyCredentials(userid: Long): CredentialAccount
    suspend fun registerAccount(userCreateDto: UserCreateDto): Unit
}

@Service
class AccountApiServiceImpl(
    private val accountService: AccountService,
    private val transaction: Transaction,
    private val userService: UserService
) :
    AccountApiService {
    override suspend fun verifyCredentials(userid: Long): CredentialAccount = transaction.transaction {
        val account = accountService.findById(userid)
        from(account)
    }

    override suspend fun registerAccount(userCreateDto: UserCreateDto) {
        userService.createLocalUser(UserCreateDto(userCreateDto.name, userCreateDto.name, "", userCreateDto.password))
    }

    private fun from(account: Account): CredentialAccount {
        return CredentialAccount(
            id = account.id,
            username = account.username,
            acct = account.acct,
            url = account.url,
            displayName = account.displayName,
            note = account.note,
            avatar = account.avatar,
            avatarStatic = account.avatarStatic,
            header = account.header,
            headerStatic = account.headerStatic,
            locked = account.locked,
            fields = account.fields,
            emojis = account.emojis,
            bot = account.bot,
            group = account.group,
            discoverable = account.discoverable,
            createdAt = account.createdAt,
            lastStatusAt = account.lastStatusAt,
            statusesCount = account.statusesCount,
            followersCount = account.followersCount,
            noindex = account.noindex,
            moved = account.moved,
            suspendex = account.suspendex,
            limited = account.limited,
            followingCount = account.followingCount,
            source = CredentialAccountSource(
                account.note,
                account.fields,
                CredentialAccountSource.Privacy.public,
                false,
                0
            ),
            role = Role(0, "Admin", "", 32)
        )
    }
}
