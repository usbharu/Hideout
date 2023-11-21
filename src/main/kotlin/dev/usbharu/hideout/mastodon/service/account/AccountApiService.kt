package dev.usbharu.hideout.mastodon.service.account

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.user.UserRepository
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.service.user.UserCreateDto
import dev.usbharu.hideout.core.service.user.UserService
import dev.usbharu.hideout.domain.mastodon.model.generated.*
import org.springframework.stereotype.Service

@Service
interface AccountApiService {
    suspend fun verifyCredentials(userid: Long): CredentialAccount
    suspend fun registerAccount(userCreateDto: UserCreateDto): Unit
    suspend fun follow(userid: Long, followeeId: Long): Relationship
    suspend fun account(id: Long): Account
}

@Service
class AccountApiServiceImpl(
    private val accountService: AccountService,
    private val transaction: Transaction,
    private val userService: UserService,
    private val followerQueryService: FollowerQueryService,
    private val userRepository: UserRepository
) :
    AccountApiService {
    override suspend fun verifyCredentials(userid: Long): CredentialAccount = transaction.transaction {
        val account = accountService.findById(userid)
        from(account)
    }

    override suspend fun registerAccount(userCreateDto: UserCreateDto) {
        userService.createLocalUser(UserCreateDto(userCreateDto.name, userCreateDto.name, "", userCreateDto.password))
    }

    override suspend fun follow(userid: Long, followeeId: Long): Relationship = transaction.transaction {

        val alreadyFollow = followerQueryService.alreadyFollow(followeeId, userid)


        val followRequest = if (alreadyFollow) {
            true
        } else {
            userService.followRequest(followeeId, userid)
        }

        val alreadyFollow1 = followerQueryService.alreadyFollow(userid, followeeId)

        val followRequestsById = userRepository.findFollowRequestsById(followeeId, userid)

        return@transaction Relationship(
            followeeId.toString(),
            followRequest,
            true,
            false,
            alreadyFollow1,
            false,
            false,
            false,
            false,
            followRequestsById,
            false,
            false,
            ""
        )
    }

    override suspend fun account(id: Long): Account = transaction.transaction {
        return@transaction accountService.findById(id)
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
