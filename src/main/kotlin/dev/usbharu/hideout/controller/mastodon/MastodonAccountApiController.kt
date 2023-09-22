package dev.usbharu.hideout.controller.mastodon

import dev.usbharu.hideout.controller.mastodon.generated.AccountApi
import dev.usbharu.hideout.domain.mastodon.model.generated.CredentialAccount
import dev.usbharu.hideout.service.api.mastodon.AccountApiService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller

@Controller
class MastodonAccountApiController(private val accountApiService: AccountApiService) : AccountApi {
    override suspend fun apiV1AccountsVerifyCredentialsGet(): ResponseEntity<CredentialAccount> {
        val principal = SecurityContextHolder.getContext().getAuthentication().principal as Jwt

        return ResponseEntity(
            accountApiService.verifyCredentials(principal.getClaim<String>("uid").toLong()),
            HttpStatus.OK
        )
    }
}
