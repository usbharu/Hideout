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

package dev.usbharu.hideout.mastodon.interfaces.api.account

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.toHttpHeader
import dev.usbharu.hideout.controller.mastodon.generated.AccountApi
import dev.usbharu.hideout.core.infrastructure.springframework.security.LoginUserContextHolder
import dev.usbharu.hideout.core.service.user.UserCreateDto
import dev.usbharu.hideout.domain.mastodon.model.generated.*
import dev.usbharu.hideout.mastodon.service.account.AccountApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import java.net.URI

@Controller
class MastodonAccountApiController(
    private val accountApiService: AccountApiService,
    private val transaction: Transaction,
    private val loginUserContextHolder: LoginUserContextHolder,
    private val applicationConfig: ApplicationConfig
) : AccountApi {

    override suspend fun apiV1AccountsIdFollowPost(
        id: String,
        followRequestBody: FollowRequestBody?
    ): ResponseEntity<Relationship> {
        val userid = loginUserContextHolder.getLoginUserId()

        return ResponseEntity.ok(accountApiService.follow(userid, id.toLong()))
    }

    override suspend fun apiV1AccountsIdGet(id: String): ResponseEntity<Account> =
        ResponseEntity.ok(accountApiService.account(id.toLong()))

    override suspend fun apiV1AccountsVerifyCredentialsGet(): ResponseEntity<CredentialAccount> = ResponseEntity(
        accountApiService.verifyCredentials(loginUserContextHolder.getLoginUserId()),
        HttpStatus.OK
    )

    override suspend fun apiV1AccountsPost(accountsCreateRequest: AccountsCreateRequest): ResponseEntity<Unit> {
        transaction.transaction {
            accountApiService.registerAccount(
                UserCreateDto(
                    accountsCreateRequest.username,
                    accountsCreateRequest.username,
                    "",
                    accountsCreateRequest.password
                )
            )
        }
        val httpHeaders = HttpHeaders()
        httpHeaders.location = URI("/users/${accountsCreateRequest.username}")
        return ResponseEntity(Unit, httpHeaders, HttpStatus.FOUND)
    }

    override fun apiV1AccountsIdStatusesGet(
        id: String,
        maxId: String?,
        sinceId: String?,
        minId: String?,
        limit: Int,
        onlyMedia: Boolean,
        excludeReplies: Boolean,
        excludeReblogs: Boolean,
        pinned: Boolean,
        tagged: String?
    ): ResponseEntity<Flow<Status>> = runBlocking {
        val userid = loginUserContextHolder.getLoginUserIdOrNull()
        val statuses = accountApiService.accountsStatuses(
            userid = id.toLong(),
            onlyMedia = onlyMedia,
            excludeReplies = excludeReplies,
            excludeReblogs = excludeReblogs,
            pinned = pinned,
            tagged = tagged,
            loginUser = userid,
            page = Page.of(
                maxId?.toLongOrNull(),
                sinceId?.toLongOrNull(),
                minId?.toLongOrNull(),
                limit.coerceIn(0, 80)
            )
        )
        val httpHeader = statuses.toHttpHeader(
            { "${applicationConfig.url}/api/v1/accounts/$id/statuses?min_id=$it" },
            { "${applicationConfig.url}/api/v1/accounts/$id/statuses?max_id=$it" },
        )

        if (httpHeader != null) {
            return@runBlocking ResponseEntity.ok().header("Link", httpHeader).body(statuses.asFlow())
        }

        ResponseEntity.ok(statuses.asFlow())
    }

    override fun apiV1AccountsRelationshipsGet(
        id: List<String>?,
        withSuspended: Boolean
    ): ResponseEntity<Flow<Relationship>> = runBlocking {
        val userid = loginUserContextHolder.getLoginUserId()

        ResponseEntity.ok(
            accountApiService.relationships(userid, id.orEmpty().mapNotNull { it.toLongOrNull() }, withSuspended)
                .asFlow()
        )
    }

    override suspend fun apiV1AccountsIdBlockPost(id: String): ResponseEntity<Relationship> {
        val userid = loginUserContextHolder.getLoginUserId()

        val block = accountApiService.block(userid, id.toLong())

        return ResponseEntity.ok(block)
    }

    override suspend fun apiV1AccountsIdUnblockPost(id: String): ResponseEntity<Relationship> {
        val userid = loginUserContextHolder.getLoginUserId()

        val unblock = accountApiService.unblock(userid, id.toLong())

        return ResponseEntity.ok(unblock)
    }

    override suspend fun apiV1AccountsIdUnfollowPost(id: String): ResponseEntity<Relationship> {
        val userid = loginUserContextHolder.getLoginUserId()

        val unfollow = accountApiService.unfollow(userid, id.toLong())

        return ResponseEntity.ok(unfollow)
    }

    override suspend fun apiV1AccountsIdRemoveFromFollowersPost(id: String): ResponseEntity<Relationship> {
        val userid = loginUserContextHolder.getLoginUserId()

        val removeFromFollowers = accountApiService.removeFromFollowers(userid, id.toLong())

        return ResponseEntity.ok(removeFromFollowers)
    }

    override suspend fun apiV1AccountsUpdateCredentialsPatch(updateCredentials: UpdateCredentials?): ResponseEntity<Account> {
        val userid = loginUserContextHolder.getLoginUserId()

        val removeFromFollowers = accountApiService.updateProfile(userid, updateCredentials)

        return ResponseEntity.ok(removeFromFollowers)
    }

    override suspend fun apiV1FollowRequestsAccountIdAuthorizePost(accountId: String): ResponseEntity<Relationship> {
        val userid = loginUserContextHolder.getLoginUserId()

        val acceptFollowRequest = accountApiService.acceptFollowRequest(userid, accountId.toLong())

        return ResponseEntity.ok(acceptFollowRequest)
    }

    override suspend fun apiV1FollowRequestsAccountIdRejectPost(accountId: String): ResponseEntity<Relationship> {
        val userid = loginUserContextHolder.getLoginUserId()

        val rejectFollowRequest = accountApiService.rejectFollowRequest(userid, accountId.toLong())

        return ResponseEntity.ok(rejectFollowRequest)
    }

    override fun apiV1FollowRequestsGet(maxId: String?, sinceId: String?, limit: Int?): ResponseEntity<Flow<Account>> =
        runBlocking {
            val userid = loginUserContextHolder.getLoginUserId()

            val followRequests = accountApiService.followRequests(
                userid,
                false,
                Page.PageByMaxId(
                    maxId?.toLongOrNull(),
                    sinceId?.toLongOrNull(),
                    limit?.coerceIn(0, 80) ?: 40
                )

            )

            val httpHeader = followRequests.toHttpHeader(
                { "${applicationConfig.url}/api/v1/follow_requests?max_id=$it" },
                { "${applicationConfig.url}/api/v1/follow_requests?min_id=$it" },
            )

            if (httpHeader != null) {
                return@runBlocking ResponseEntity.ok().header("Link", httpHeader).body(followRequests.asFlow())
            }

            ResponseEntity.ok(followRequests.asFlow())
        }

    override suspend fun apiV1AccountsIdMutePost(id: String): ResponseEntity<Relationship> {
        val userid = loginUserContextHolder.getLoginUserId()

        val mute = accountApiService.mute(userid, id.toLong())

        return ResponseEntity.ok(mute)
    }

    override suspend fun apiV1AccountsIdUnmutePost(id: String): ResponseEntity<Relationship> {
        val userid = loginUserContextHolder.getLoginUserId()

        val unmute = accountApiService.unmute(userid, id.toLong())

        return ResponseEntity.ok(unmute)
    }

    override fun apiV1MutesGet(maxId: String?, sinceId: String?, limit: Int?): ResponseEntity<Flow<Account>> =
        runBlocking {
            val userid = loginUserContextHolder.getLoginUserId()

            val mutes =
                accountApiService.mutesAccount(
                    userid,
                    Page.PageByMaxId(maxId?.toLongOrNull(), sinceId?.toLongOrNull(), limit?.coerceIn(0, 80) ?: 40)
                )

            val httpHeader = mutes.toHttpHeader(
                { "${applicationConfig.url}/api/v1/mutes?max_id=$it" },
                { "${applicationConfig.url}/api/v1/mutes?since_id=$it" },
            )

            if (httpHeader != null) {
                return@runBlocking ResponseEntity.ok().header("Link", httpHeader).body(mutes.asFlow())
            }

            return@runBlocking ResponseEntity.ok(mutes.asFlow())
        }
}
