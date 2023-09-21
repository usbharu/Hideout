package dev.usbharu.hideout.service.api.mastodon

import dev.usbharu.hideout.domain.mastodon.model.generated.Application
import dev.usbharu.hideout.domain.mastodon.model.generated.AppsRequest
import dev.usbharu.hideout.service.auth.SecureTokenGenerator
import dev.usbharu.hideout.service.core.Transaction
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.stereotype.Service
import java.util.*

@Service
interface AppApiService {
    suspend fun createApp(appsRequest: AppsRequest): Application
}

@Service
class AppApiServiceImpl(
    private val registeredClientRepository: RegisteredClientRepository,
    private val secureTokenGenerator: SecureTokenGenerator,
    private val passwordEncoder: PasswordEncoder,
    private val transaction: Transaction
) : AppApiService {
    override suspend fun createApp(appsRequest: AppsRequest): Application {
        return transaction.transaction {
            val id = UUID.randomUUID().toString()
            val clientSecret = secureTokenGenerator.generate()
            val registeredClient = RegisteredClient.withId(id)
                .clientId(id)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientName(appsRequest.clientName)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_JWT)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(appsRequest.redirectUris)
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .scopes { it.addAll(parseScope(appsRequest.scopes.orEmpty())) }
                .build()
            registeredClientRepository.save(registeredClient)

            Application(
                appsRequest.clientName,
                "invalid-vapid-key",
                appsRequest.website,
                id,
                clientSecret
            )
        }
    }

    private fun parseScope(string: String): Set<String> {
        return string.split(" ").toSet()
    }
}
