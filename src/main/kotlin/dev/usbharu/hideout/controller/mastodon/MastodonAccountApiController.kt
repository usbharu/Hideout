package dev.usbharu.hideout.controller.mastodon

import dev.usbharu.hideout.controller.mastodon.generated.AccountApi
import dev.usbharu.hideout.domain.mastodon.model.generated.CredentialAccount
import dev.usbharu.hideout.domain.model.hideout.dto.UserCreateDto
import dev.usbharu.hideout.service.api.mastodon.AccountApiService
import dev.usbharu.hideout.service.core.Transaction
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
    override fun apiV1AccountsVerifyCredentialsGet(): ResponseEntity<CredentialAccount> = runBlocking {
        val principal = SecurityContextHolder.getContext().getAuthentication().principal as Jwt

        ResponseEntity(
            accountApiService.verifyCredentials(principal.getClaim<String>("uid").toLong()),
            HttpStatus.OK
        )
    }

    override fun apiV1AccountsPost(
        username: String,
        password: String,
        email: String?,
        agreement: Boolean?,
        locale: Boolean?,
        reason: String?
    ): ResponseEntity<Unit> = runBlocking {
        transaction.transaction {
            accountApiService.registerAccount(UserCreateDto(username, username, "", password))
        }
        val httpHeaders = HttpHeaders()
        httpHeaders.location = URI("/users/$username")
        ResponseEntity(Unit, httpHeaders, HttpStatus.FOUND)
    }
}
