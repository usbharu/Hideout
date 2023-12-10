package dev.usbharu.hideout.mastodon.interfaces.api.account

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.controller.mastodon.generated.AccountApi
import dev.usbharu.hideout.core.service.user.UserCreateDto
import dev.usbharu.hideout.domain.mastodon.model.generated.*
import dev.usbharu.hideout.mastodon.service.account.AccountApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import java.net.URI

@Controller
class MastodonAccountApiController(
    private val accountApiService: AccountApiService,
    private val transaction: Transaction
) : AccountApi {

    override suspend fun apiV1AccountsIdFollowPost(
        id: String,
        followRequestBody: FollowRequestBody?
    ): ResponseEntity<Relationship> {
        val principal = SecurityContextHolder.getContext().getAuthentication().principal as Jwt

        return ResponseEntity.ok(accountApiService.follow(principal.getClaim<String>("uid").toLong(), id.toLong()))
    }

    override suspend fun apiV1AccountsIdGet(id: String): ResponseEntity<Account> =
        ResponseEntity.ok(accountApiService.account(id.toLong()))

    override suspend fun apiV1AccountsVerifyCredentialsGet(): ResponseEntity<CredentialAccount> {
        val principal = SecurityContextHolder.getContext().getAuthentication().principal as Jwt

        return ResponseEntity(
            accountApiService.verifyCredentials(principal.getClaim<String>("uid").toLong()),
            HttpStatus.OK
        )
    }

    override suspend fun apiV1AccountsPost(
        username: String,
        password: String,
        email: String?,
        agreement: Boolean?,
        locale: Boolean?,
        reason: String?
    ): ResponseEntity<Unit> {
        transaction.transaction {
            accountApiService.registerAccount(UserCreateDto(username, username, "", password))
        }
        val httpHeaders = HttpHeaders()
        httpHeaders.location = URI("/users/$username")
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
        val principal = SecurityContextHolder.getContext().getAuthentication().principal as Jwt

        val userid = principal.getClaim<String>("uid").toLong()
        val statusFlow = accountApiService.accountsStatuses(
            userid = id.toLong(),
            maxId = maxId?.toLongOrNull(),
            sinceId = sinceId?.toLongOrNull(),
            minId = minId?.toLongOrNull(),
            limit = limit,
            onlyMedia = onlyMedia,
            excludeReplies = excludeReplies,
            excludeReblogs = excludeReblogs,
            pinned = pinned,
            tagged = tagged,
            loginUser = userid
        ).asFlow()
        ResponseEntity.ok(statusFlow)
    }

    override fun apiV1AccountsRelationshipsGet(
        id: List<String>?,
        withSuspended: Boolean
    ): ResponseEntity<Flow<Relationship>> = runBlocking {
        val principal = SecurityContextHolder.getContext().getAuthentication().principal as Jwt

        val userid = principal.getClaim<String>("uid").toLong()

        ResponseEntity.ok(
            accountApiService.relationships(userid, id.orEmpty().mapNotNull { it.toLongOrNull() }, withSuspended)
                .asFlow()
        )
    }

    override suspend fun apiV1AccountsIdBlockPost(id: String): ResponseEntity<Relationship> {
        val principal = SecurityContextHolder.getContext().getAuthentication().principal as Jwt

        val userid = principal.getClaim<String>("uid").toLong()

        val block = accountApiService.block(userid, id.toLong())

        return ResponseEntity.ok(block)
    }
}
