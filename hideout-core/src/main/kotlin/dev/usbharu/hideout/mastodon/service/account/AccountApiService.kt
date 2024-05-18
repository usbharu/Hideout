/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.mastodon.service.account

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.service.media.MediaService
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import dev.usbharu.hideout.core.service.user.UpdateUserDto
import dev.usbharu.hideout.core.service.user.UserCreateDto
import dev.usbharu.hideout.core.service.user.UserService
import dev.usbharu.hideout.domain.mastodon.model.generated.*
import dev.usbharu.hideout.mastodon.domain.exception.AccountNotFoundException
import dev.usbharu.hideout.mastodon.interfaces.api.media.MediaRequest
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.min

@Service
@Suppress("TooManyFunctions")
interface AccountApiService {

    @Suppress("LongParameterList")
    suspend fun accountsStatuses(
        userid: Long,
        onlyMedia: Boolean,
        excludeReplies: Boolean,
        excludeReblogs: Boolean,
        pinned: Boolean,
        tagged: String?,
        loginUser: Long?,
        page: Page
    ): PaginationList<Status, Long>

    suspend fun verifyCredentials(userid: Long): CredentialAccount
    suspend fun registerAccount(userCreateDto: UserCreateDto): Unit
    suspend fun follow(loginUser: Long, followTargetUserId: Long): Relationship
    suspend fun account(id: Long): Account
    suspend fun relationships(userid: Long, id: List<Long>, withSuspended: Boolean): List<Relationship>

    /**
     * ブロック操作を行う
     *
     * @param userid ブロック操作を行ったユーザーid
     * @param target ブロック対象のユーザーid
     * @return ブロック後のブロック対象ユーザーとの[Relationship]
     */
    suspend fun block(userid: Long, target: Long): Relationship
    suspend fun unblock(userid: Long, target: Long): Relationship
    suspend fun unfollow(userid: Long, target: Long): Relationship
    suspend fun removeFromFollowers(userid: Long, target: Long): Relationship
    suspend fun updateProfile(userid: Long, updateCredentials: UpdateCredentials?): Account

    suspend fun followRequests(
        loginUser: Long,
        withIgnore: Boolean,
        pageByMaxId: Page.PageByMaxId
    ): PaginationList<Account, Long>

    suspend fun acceptFollowRequest(loginUser: Long, target: Long): Relationship
    suspend fun rejectFollowRequest(loginUser: Long, target: Long): Relationship
    suspend fun mute(userid: Long, target: Long): Relationship
    suspend fun unmute(userid: Long, target: Long): Relationship
    suspend fun mutesAccount(userid: Long, pageByMaxId: Page.PageByMaxId): PaginationList<Account, Long>
}

@Service
class AccountApiServiceImpl(
    private val accountService: AccountService,
    private val transaction: Transaction,
    private val userService: UserService,
    private val statusQueryService: StatusQueryService,
    private val relationshipService: RelationshipService,
    private val relationshipRepository: RelationshipRepository,
    private val mediaService: MediaService
) :
    AccountApiService {

    override suspend fun accountsStatuses(
        userid: Long,
        onlyMedia: Boolean,
        excludeReplies: Boolean,
        excludeReblogs: Boolean,
        pinned: Boolean,
        tagged: String?,
        loginUser: Long?,
        page: Page
    ): PaginationList<Status, Long> {
        val canViewFollowers = if (loginUser == null) {
            false
        } else if (loginUser == userid) {
            true
        } else {
            transaction.transaction {
                isFollowing(loginUser, userid)
            }
        }

        return transaction.transaction {
            statusQueryService.accountsStatus(
                accountId = userid,
                onlyMedia = onlyMedia,
                excludeReplies = excludeReplies,
                excludeReblogs = excludeReblogs,
                pinned = pinned,
                tagged = tagged,
                includeFollowers = canViewFollowers,
                page = page
            )
        }
    }

    override suspend fun verifyCredentials(userid: Long): CredentialAccount = transaction.transaction {
        userService.updateUserStatistics(userid)

        val account = accountService.findById(userid)
        from(account)
    }

    override suspend fun registerAccount(userCreateDto: UserCreateDto) {
        userService.createLocalUser(UserCreateDto(userCreateDto.name, userCreateDto.name, "", userCreateDto.password))
    }

    override suspend fun follow(loginUser: Long, followTargetUserId: Long): Relationship = transaction.transaction {
        relationshipService.followRequest(loginUser, followTargetUserId)

        return@transaction fetchRelationship(loginUser, followTargetUserId)
    }

    override suspend fun account(id: Long): Account {
        return try {
            transaction.transaction {
                userService.updateUserStatistics(id)
                return@transaction accountService.findById(id)
            }
        } catch (_: UserNotFoundException) {
            throw AccountNotFoundException.ofId(id)
        }
    }

    override suspend fun relationships(userid: Long, id: List<Long>, withSuspended: Boolean): List<Relationship> =
        transaction.transaction {
            if (id.isEmpty()) {
                return@transaction emptyList()
            }

            logger.warn("id is too long! ({}) truncate to 20", id.size)

            val subList = id.subList(0, min(id.size, 20))

            return@transaction subList.map {
                fetchRelationship(userid, it)
            }
        }

    override suspend fun block(userid: Long, target: Long) = transaction.transaction {
        relationshipService.block(userid, target)

        fetchRelationship(userid, target)
    }

    override suspend fun unblock(userid: Long, target: Long): Relationship = transaction.transaction {
        relationshipService.unblock(userid, target)

        return@transaction fetchRelationship(userid, target)
    }

    override suspend fun unfollow(userid: Long, target: Long): Relationship = transaction.transaction {
        relationshipService.unfollow(userid, target)

        return@transaction fetchRelationship(userid, target)
    }

    override suspend fun removeFromFollowers(userid: Long, target: Long): Relationship = transaction.transaction {
        relationshipService.rejectFollowRequest(userid, target)

        return@transaction fetchRelationship(userid, target)
    }

    override suspend fun updateProfile(userid: Long, updateCredentials: UpdateCredentials?): Account =
        transaction.transaction {
            val avatarMedia = if (updateCredentials?.avatar != null) {
                mediaService.uploadLocalMedia(
                    MediaRequest(
                        updateCredentials.avatar,
                        null,
                        null,
                        null
                    )
                )
            } else {
                null
            }

            val headerMedia = if (updateCredentials?.header != null) {
                mediaService.uploadLocalMedia(
                    MediaRequest(
                        updateCredentials.header,
                        null,
                        null,
                        null
                    )
                )
            } else {
                null
            }

            val account = accountService.findById(userid)

            val updateUserDto = UpdateUserDto(
                screenName = updateCredentials?.displayName ?: account.displayName,
                description = updateCredentials?.note ?: account.note,
                avatarMedia = avatarMedia,
                headerMedia = headerMedia,
                locked = updateCredentials?.locked ?: account.locked,
                autoAcceptFolloweeFollowRequest = false
            )
            userService.updateUser(userid, updateUserDto)

            accountService.findById(userid)
        }

    override suspend fun followRequests(
        loginUser: Long,
        withIgnore: Boolean,
        pageByMaxId: Page.PageByMaxId
    ): PaginationList<Account, Long> = transaction.transaction {
        val request =
            relationshipRepository.findByTargetIdAndFollowRequestAndIgnoreFollowRequest(
                loginUser,
                true,
                withIgnore,
                pageByMaxId
            )
        val actorIds = request.map { it.actorId }

        return@transaction PaginationList(accountService.findByIds(actorIds), request.next, request.prev)
    }

    override suspend fun acceptFollowRequest(loginUser: Long, target: Long): Relationship = transaction.transaction {
        relationshipService.acceptFollowRequest(loginUser, target)

        return@transaction fetchRelationship(loginUser, target)
    }

    override suspend fun rejectFollowRequest(loginUser: Long, target: Long): Relationship = transaction.transaction {
        relationshipService.rejectFollowRequest(loginUser, target)

        return@transaction fetchRelationship(loginUser, target)
    }

    override suspend fun mute(userid: Long, target: Long): Relationship = transaction.transaction {
        relationshipService.mute(userid, target)

        return@transaction fetchRelationship(userid, target)
    }

    override suspend fun unmute(userid: Long, target: Long): Relationship = transaction.transaction {
        relationshipService.mute(userid, target)

        return@transaction fetchRelationship(userid, target)
    }

    override suspend fun mutesAccount(userid: Long, pageByMaxId: Page.PageByMaxId): PaginationList<Account, Long> {
        val mutedAccounts = relationshipRepository.findByActorIdAndMuting(userid, true, pageByMaxId)

        return PaginationList(
            accountService.findByIds(mutedAccounts.map { it.targetActorId }),
            mutedAccounts.next,
            mutedAccounts.prev
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
            source = AccountSource(
                account.note,
                account.fields,
                AccountSource.Privacy.public,
                false,
                0
            ),
            role = Role(0, "Admin", "", 32)
        )
    }

    private suspend fun fetchRelationship(userid: Long, targetId: Long): Relationship {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(userid, targetId)
            ?: dev.usbharu.hideout.core.domain.model.relationship.Relationship(
                actorId = userid,
                targetActorId = targetId,
                following = false,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestToTarget = false
            )

        val inverseRelationship = relationshipRepository.findByUserIdAndTargetUserId(targetId, userid)
            ?: dev.usbharu.hideout.core.domain.model.relationship.Relationship(
                actorId = targetId,
                targetActorId = userid,
                following = false,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestToTarget = false
            )

        userService.updateUserStatistics(userid)
        userService.updateUserStatistics(targetId)

        return Relationship(
            id = targetId.toString(),
            following = relationship.following,
            showingReblogs = true,
            notifying = false,
            followedBy = inverseRelationship.following,
            blocking = relationship.blocking,
            blockedBy = inverseRelationship.blocking,
            muting = relationship.muting,
            mutingNotifications = relationship.muting,
            requested = relationship.followRequest,
            domainBlocking = false,
            endorsed = false,
            note = ""
        )
    }

    private suspend fun isFollowing(userid: Long, target: Long): Boolean =
        relationshipRepository.findByUserIdAndTargetUserId(userid, target)?.following ?: false

    companion object {
        private val logger = LoggerFactory.getLogger(AccountApiServiceImpl::class.java)
    }
}
