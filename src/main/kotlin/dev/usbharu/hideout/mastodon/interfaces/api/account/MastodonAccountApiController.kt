package dev.usbharu.hideout.mastodon.interfaces.api.account

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.controller.mastodon.generated.AccountApi
import dev.usbharu.hideout.core.service.user.UserCreateDto
import dev.usbharu.hideout.domain.mastodon.model.generated.CredentialAccount
import dev.usbharu.hideout.mastodon.service.account.AccountApiService
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
}
