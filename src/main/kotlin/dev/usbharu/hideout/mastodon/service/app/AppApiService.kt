package dev.usbharu.hideout.mastodon.service.app

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.SecureTokenGenerator
import dev.usbharu.hideout.domain.mastodon.model.generated.Application
import dev.usbharu.hideout.domain.mastodon.model.generated.AppsRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
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
                .tokenSettings(
                    TokenSettings.builder()
                        .accessTokenTimeToLive(
                            Duration.ofSeconds((Instant.MAX.epochSecond - Instant.now().epochSecond - 10000) / 1000)
                        )
                        .build()
                )
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


        return string.split(" ")
            .flatMap {
                when (it) {
                    "read" -> READ_SCOPES
                    "write" -> WRITE_SCOPES
                    "follow" -> FOLLOW_SCOPES
                    "admin" -> ADMIN_SCOPES
                    "admin:write" -> ADMIN_WRITE_SCOPES
                    "admin:read" -> ADMIN_READ_SCOPES
                    else -> listOfNotNull(it.takeIf { ALL_SCOPES.contains(it) })
                }
            }
            .toSet()
    }

    companion object {
        private val READ_SCOPES = listOf(
            "read:accounts",
            "read:blocks",
            "read:bookmarks",
            "read:favourites",
            "read:filters",
            "read:follows",
            "read:lists",
            "read:mutes",
            "read:notifications",
            "read:search",
            "read:statuses"
        )

        private val WRITE_SCOPES = listOf(
            "write:accounts",
            "write:blocks",
            "write:bookmarks",
            "write:conversations",
            "write:favourites",
            "write:filters",
            "write:follows",
            "write:lists",
            "write:media",
            "write:mutes",
            "write:notifications",
            "write:reports",
            "write:statuses"
        )

        private val FOLLOW_SCOPES = listOf(
            "read:blocks",
            "write:blocks",
            "read:follows",
            "write:follows",
            "read:mutes",
            "write:mutes"
        )

        private val ADMIN_READ_SCOPES = listOf(
            "admin:read:accounts",
            "admin:read:reports",
            "admin:read:domain_allows",
            "admin:read:domain_blocks",
            "admin:read:ip_blocks",
            "admin:read:email_domain_blocks",
            "admin:read:canonical_email_blocks"
        )

        private val ADMIN_WRITE_SCOPES = listOf(
            "admin:write:accounts",
            "admin:write:reports",
            "admin:write:domain_allows",
            "admin:write:domain_blocks",
            "admin:write:ip_blocks",
            "admin:write:email_domain_blocks",
            "admin:write:canonical_email_blocks"
        )

        private val ADMIN_SCOPES = ADMIN_READ_SCOPES + ADMIN_WRITE_SCOPES

        private val ALL_SCOPES = READ_SCOPES + WRITE_SCOPES + FOLLOW_SCOPES + ADMIN_SCOPES
    }
}
