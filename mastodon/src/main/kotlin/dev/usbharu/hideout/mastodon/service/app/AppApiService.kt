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
                            Duration.ofSeconds(31536000000)
                        )
                        .build()
                )
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .scopes { it.addAll(parseScope(appsRequest.scopes.orEmpty())) }
                .build()
            registeredClientRepository.save(registeredClient)

            Application(
                name = appsRequest.clientName,
                vapidKey = "invalid-vapid-key",
                website = appsRequest.website,
                clientId = id,
                clientSecret = clientSecret,
                redirectUri = appsRequest.redirectUris
            )
        }
    }

    private fun parseScope(string: String): Set<String> = string.split(" ").toSet()
}
