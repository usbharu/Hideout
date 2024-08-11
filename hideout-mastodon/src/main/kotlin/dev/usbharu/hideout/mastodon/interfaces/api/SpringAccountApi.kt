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

package dev.usbharu.hideout.mastodon.interfaces.api

import dev.usbharu.hideout.core.application.actor.GetUserDetail
import dev.usbharu.hideout.core.application.actor.GetUserDetailApplicationService
import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.application.relationship.acceptfollowrequest.AcceptFollowRequest
import dev.usbharu.hideout.core.application.relationship.acceptfollowrequest.UserAcceptFollowRequestApplicationService
import dev.usbharu.hideout.core.application.relationship.block.Block
import dev.usbharu.hideout.core.application.relationship.block.UserBlockApplicationService
import dev.usbharu.hideout.core.application.relationship.followrequest.FollowRequest
import dev.usbharu.hideout.core.application.relationship.followrequest.UserFollowRequestApplicationService
import dev.usbharu.hideout.core.application.relationship.get.GetRelationship
import dev.usbharu.hideout.core.application.relationship.get.GetRelationshipApplicationService
import dev.usbharu.hideout.core.application.relationship.mute.Mute
import dev.usbharu.hideout.core.application.relationship.mute.UserMuteApplicationService
import dev.usbharu.hideout.core.application.relationship.rejectfollowrequest.RejectFollowRequest
import dev.usbharu.hideout.core.application.relationship.rejectfollowrequest.UserRejectFollowRequestApplicationService
import dev.usbharu.hideout.core.application.relationship.removefromfollowers.RemoveFromFollowers
import dev.usbharu.hideout.core.application.relationship.removefromfollowers.UserRemoveFromFollowersApplicationService
import dev.usbharu.hideout.core.application.relationship.unblock.Unblock
import dev.usbharu.hideout.core.application.relationship.unblock.UserUnblockApplicationService
import dev.usbharu.hideout.core.application.relationship.unfollow.Unfollow
import dev.usbharu.hideout.core.application.relationship.unfollow.UserUnfollowApplicationService
import dev.usbharu.hideout.core.application.relationship.unmute.Unmute
import dev.usbharu.hideout.core.application.relationship.unmute.UserUnmuteApplicationService
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.SpringSecurityOauth2PrincipalContextHolder
import dev.usbharu.hideout.mastodon.application.accounts.GetAccount
import dev.usbharu.hideout.mastodon.application.accounts.GetAccountApplicationService
import dev.usbharu.hideout.mastodon.interfaces.api.generated.AccountApi
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.*
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class SpringAccountApi(
    private val getUserDetailApplicationService: GetUserDetailApplicationService,
    private val getAccountApplicationService: GetAccountApplicationService,
    private val userFollowRequestApplicationService: UserFollowRequestApplicationService,
    private val getRelationshipApplicationService: GetRelationshipApplicationService,
    private val userBlockApplicationService: UserBlockApplicationService,
    private val userUnblockApplicationService: UserUnblockApplicationService,
    private val userMuteApplicationService: UserMuteApplicationService,
    private val userUnmuteApplicationService: UserUnmuteApplicationService,
    private val userAcceptFollowRequestApplicationService: UserAcceptFollowRequestApplicationService,
    private val userRejectFollowRequestApplicationService: UserRejectFollowRequestApplicationService,
    private val userRemoveFromFollowersApplicationService: UserRemoveFromFollowersApplicationService,
    private val userUnfollowApplicationService: UserUnfollowApplicationService,
    private val principalContextHolder: SpringSecurityOauth2PrincipalContextHolder
) : AccountApi {


    override suspend fun apiV1AccountsIdBlockPost(id: String): ResponseEntity<Relationship> {
        userBlockApplicationService.execute(Block(id.toLong()), principalContextHolder.getPrincipal())
        return fetchRelationship(id)
    }

    override suspend fun apiV1AccountsIdFollowPost(
        id: String,
        followRequestBody: FollowRequestBody?,
    ): ResponseEntity<Relationship> {

        userFollowRequestApplicationService.execute(
            FollowRequest(id.toLong()), principalContextHolder.getPrincipal()
        )
        return fetchRelationship(id)
    }

    private suspend fun fetchRelationship(
        id: String,
    ): ResponseEntity<Relationship> {
        val relationship = getRelationshipApplicationService.execute(
            GetRelationship(id.toLong()),
            principalContextHolder.getPrincipal()
        )
        return ResponseEntity.ok(
            Relationship(
                id = relationship.targetId.toString(),
                following = relationship.following,
                showingReblogs = true,
                notifying = false,
                followedBy = relationship.followedBy,
                blocking = relationship.blocking,
                blockedBy = relationship.blockedBy,
                muting = relationship.muting,
                mutingNotifications = false,
                requested = relationship.followRequesting,
                domainBlocking = relationship.domainBlocking,
                endorsed = false,
                note = ""
            )
        )
    }

    override suspend fun apiV1AccountsIdGet(id: String): ResponseEntity<Account> {
        return ResponseEntity.ok(
            getAccountApplicationService.execute(
                GetAccount(id), principalContextHolder.getPrincipal()
            )
        )
    }

    override suspend fun apiV1AccountsIdMutePost(id: String): ResponseEntity<Relationship> {
        userMuteApplicationService.execute(
            Mute(id.toLong()), principalContextHolder.getPrincipal()
        )
        return fetchRelationship(id)
    }

    override suspend fun apiV1AccountsIdRemoveFromFollowersPost(id: String): ResponseEntity<Relationship> {
        userRemoveFromFollowersApplicationService.execute(
            RemoveFromFollowers(id.toLong()), principalContextHolder.getPrincipal()
        )
        return fetchRelationship(id)
    }

    override suspend fun apiV1AccountsIdUnblockPost(id: String): ResponseEntity<Relationship> {
        userUnblockApplicationService.execute(
            Unblock(id.toLong()), principalContextHolder.getPrincipal()
        )
        return fetchRelationship(id)
    }

    override suspend fun apiV1AccountsIdUnfollowPost(id: String): ResponseEntity<Relationship> {
        userUnfollowApplicationService.execute(
            Unfollow(id.toLong()), principalContextHolder.getPrincipal()
        )
        return fetchRelationship(id)
    }

    override suspend fun apiV1AccountsIdUnmutePost(id: String): ResponseEntity<Relationship> {
        userUnmuteApplicationService.execute(
            Unmute(id.toLong()), principalContextHolder.getPrincipal()
        )
        return fetchRelationship(id)
    }

    override suspend fun apiV1AccountsPost(accountsCreateRequest: AccountsCreateRequest): ResponseEntity<Unit> {
        return super.apiV1AccountsPost(accountsCreateRequest)
    }

    override suspend fun apiV1AccountsUpdateCredentialsPatch(updateCredentials: UpdateCredentials?): ResponseEntity<Account> {
        return super.apiV1AccountsUpdateCredentialsPatch(updateCredentials)
    }

    override suspend fun apiV1AccountsVerifyCredentialsGet(): ResponseEntity<CredentialAccount> {
        val principal = principalContextHolder.getPrincipal()
        val localActor =
            getUserDetailApplicationService.execute(
                GetUserDetail(
                    principal.userDetailId?.id ?: throw PermissionDeniedException()
                ), principal
            )

        return ResponseEntity.ok(
            CredentialAccount(
                id = localActor.id.toString(),
                username = localActor.name,
                acct = localActor.name + "@" + localActor.domain,
                url = localActor.url,
                displayName = localActor.screenName,
                note = localActor.description,
                avatar = localActor.iconUrl,
                avatarStatic = localActor.iconUrl,
                header = localActor.iconUrl,
                headerStatic = localActor.iconUrl,
                locked = localActor.locked,
                fields = emptyList(),
                emojis = localActor.emojis.map {
                    CustomEmoji(
                        shortcode = it.name,
                        url = it.url.toString(),
                        staticUrl = it.url.toString(),
                        true,
                        category = it.category.orEmpty()
                    )
                },
                bot = false,
                group = false,
                discoverable = true,
                createdAt = localActor.createdAt.toString(),
                lastStatusAt = localActor.lastPostAt?.toString(),
                statusesCount = localActor.postsCount,
                followersCount = localActor.followersCount,
                followingCount = localActor.followingCount,
                moved = localActor.moveTo != null,
                noindex = true,
                suspendex = localActor.suspend,
                limited = false,
                role = null,
                source = AccountSource(
                    localActor.description,
                    emptyList(),
                    AccountSource.Privacy.`public`,
                    false,
                    0
                )
            )
        )
    }

    override suspend fun apiV1FollowRequestsAccountIdAuthorizePost(accountId: String): ResponseEntity<Relationship> {

        userAcceptFollowRequestApplicationService.execute(
            AcceptFollowRequest(accountId.toLong()), principalContextHolder.getPrincipal()
        )
        return fetchRelationship(accountId)
    }

    override suspend fun apiV1FollowRequestsAccountIdRejectPost(accountId: String): ResponseEntity<Relationship> {

        userRejectFollowRequestApplicationService.execute(
            RejectFollowRequest(accountId.toLong()), principalContextHolder.getPrincipal()
        )
        return fetchRelationship(accountId)
    }

}