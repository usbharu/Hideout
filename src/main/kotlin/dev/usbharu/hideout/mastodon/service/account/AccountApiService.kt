package dev.usbharu.hideout.mastodon.service.account

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.block.BlockRepository
import dev.usbharu.hideout.core.domain.model.user.UserRepository
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.service.block.BlockService
import dev.usbharu.hideout.core.service.user.UserCreateDto
import dev.usbharu.hideout.core.service.user.UserService
import dev.usbharu.hideout.domain.mastodon.model.generated.*
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.min

@Service
interface AccountApiService {
    suspend fun accountsStatuses(
        userid: Long,
        maxId: Long?,
        sinceId: Long?,
        minId: Long?,
        limit: Int,
        onlyMedia: Boolean,
        excludeReplies: Boolean,
        excludeReblogs: Boolean,
        pinned: Boolean,
        tagged: String?,
        loginUser: Long?
    ): List<Status>

    suspend fun verifyCredentials(userid: Long): CredentialAccount
    suspend fun registerAccount(userCreateDto: UserCreateDto): Unit
    suspend fun follow(userid: Long, followeeId: Long): Relationship
    suspend fun account(id: Long): Account
    suspend fun relationships(userid: Long, id: List<Long>, withSuspended: Boolean): List<Relationship>
    suspend fun block(userid: Long, target: Long): Relationship
}

@Service
class AccountApiServiceImpl(
    private val accountService: AccountService,
    private val transaction: Transaction,
    private val userService: UserService,
    private val followerQueryService: FollowerQueryService,
    private val userRepository: UserRepository,
    private val statusQueryService: StatusQueryService,
    private val blockService: BlockService,
    private val blockRepository: BlockRepository
) :
    AccountApiService {
    override suspend fun accountsStatuses(
        userid: Long,
        maxId: Long?,
        sinceId: Long?,
        minId: Long?,
        limit: Int,
        onlyMedia: Boolean,
        excludeReplies: Boolean,
        excludeReblogs: Boolean,
        pinned: Boolean,
        tagged: String?,
        loginUser: Long?
    ): List<Status> {
        val canViewFollowers = if (loginUser == null) {
            false
        } else {
            transaction.transaction {
                followerQueryService.alreadyFollow(userid, loginUser)
            }
        }

        return transaction.transaction {
            statusQueryService.accountsStatus(
                userid,
                maxId,
                sinceId,
                minId,
                limit,
                onlyMedia,
                excludeReplies,
                excludeReblogs,
                pinned,
                tagged,
                canViewFollowers
            )
        }
    }

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

    override suspend fun relationships(userid: Long, id: List<Long>, withSuspended: Boolean): List<Relationship> =
        transaction.transaction {
            if (id.isEmpty()) {
                return@transaction emptyList()
            }

            logger.warn("id is too long! ({}) truncate to 20", id.size)

            val subList = id.subList(0, min(id.size, 20))

            return@transaction subList.map {
                val alreadyFollow = followerQueryService.alreadyFollow(userid, it)

                val followed = followerQueryService.alreadyFollow(it, userid)

                val requested = userRepository.findFollowRequestsById(it, userid)

                Relationship(
                    id = it.toString(),
                    following = alreadyFollow,
                    showingReblogs = true,
                    notifying = false,
                    followedBy = followed,
                    blocking = false,
                    blockedBy = false,
                    muting = false,
                    mutingNotifications = false,
                    requested = requested,
                    domainBlocking = false,
                    endorsed = false,
                    note = ""
                )
            }
        }

    override suspend fun block(userid: Long, target: Long): Relationship = transaction.transaction {
        blockService.block(userid, target)

        val blocked = try {
            blockRepository.findByUserIdAndTarget(target, userid)
            true
        } catch (e: FailedToGetResourcesException) {
            false
        }

        val requested = userRepository.findFollowRequestsById(target, userid)

        Relationship(
            target.toString(),
            false,
            true,
            false,
            false,
            true,
            blocked,
            false,
            false,
            requested,
            false,
            false,
            ""
        )
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

    companion object {
        private val logger = LoggerFactory.getLogger(AccountApiServiceImpl::class.java)
    }
}
