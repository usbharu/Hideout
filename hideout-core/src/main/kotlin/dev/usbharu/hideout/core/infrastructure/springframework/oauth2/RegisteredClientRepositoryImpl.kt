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

package dev.usbharu.hideout.core.infrastructure.springframework.oauth2

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.RegisteredClient.clientId
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.RegisteredClient.clientSettings
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.RegisteredClient.tokenSettings
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient as SpringRegisteredClient

@Repository
class RegisteredClientRepositoryImpl : RegisteredClientRepository {

    override fun save(registeredClient: SpringRegisteredClient?) {
        requireNotNull(registeredClient)
        val singleOrNull =
            RegisteredClient.selectAll().where { RegisteredClient.id eq registeredClient.id }.singleOrNull()
        if (singleOrNull == null) {
            RegisteredClient.insert {
                it[id] = registeredClient.id
                it[clientId] = registeredClient.clientId
                it[clientIdIssuedAt] = registeredClient.clientIdIssuedAt ?: Instant.now()
                it[clientSecret] = registeredClient.clientSecret
                it[clientSecretExpiresAt] = registeredClient.clientSecretExpiresAt
                it[clientName] = registeredClient.clientName
                it[clientAuthenticationMethods] =
                    registeredClient.clientAuthenticationMethods.joinToString(",") { method -> method.value }
                it[authorizationGrantTypes] =
                    registeredClient.authorizationGrantTypes.joinToString(",") { type -> type.value }
                it[redirectUris] = registeredClient.redirectUris.joinToString(",")
                it[postLogoutRedirectUris] = registeredClient.postLogoutRedirectUris.joinToString(",")
                it[scopes] = registeredClient.scopes.joinToString(",")
                it[clientSettings] = mapToJson(registeredClient.clientSettings.settings)
                it[tokenSettings] = mapToJson(registeredClient.tokenSettings.settings)
            }
        } else {
            RegisteredClient.update({ RegisteredClient.id eq registeredClient.id }) {
                it[clientId] = registeredClient.clientId
                it[clientIdIssuedAt] = registeredClient.clientIdIssuedAt ?: Instant.now()
                it[clientSecret] = registeredClient.clientSecret
                it[clientSecretExpiresAt] = registeredClient.clientSecretExpiresAt
                it[clientName] = registeredClient.clientName
                it[clientAuthenticationMethods] = registeredClient.clientAuthenticationMethods.joinToString(",")
                it[authorizationGrantTypes] = registeredClient.authorizationGrantTypes.joinToString(",")
                it[redirectUris] = registeredClient.redirectUris.joinToString(",")
                it[postLogoutRedirectUris] = registeredClient.postLogoutRedirectUris.joinToString(",")
                it[scopes] = registeredClient.scopes.joinToString(",")
                it[clientSettings] = mapToJson(registeredClient.clientSettings.settings)
                it[tokenSettings] = mapToJson(registeredClient.tokenSettings.settings)
            }
        }
    }

    override fun findById(id: String?): SpringRegisteredClient? {
        if (id == null) {
            return null
        }
        return RegisteredClient.selectAll().where { RegisteredClient.id eq id }.singleOrNull()?.toRegisteredClient()
    }

    @Transactional
    override fun findByClientId(clientId: String?): SpringRegisteredClient? {
        if (clientId == null) {
            return null
        }
        val toRegisteredClient =
            RegisteredClient.selectAll().where { RegisteredClient.clientId eq clientId }.singleOrNull()
                ?.toRegisteredClient()
        LOGGER.trace("findByClientId: {}", toRegisteredClient)
        return toRegisteredClient
    }

    private fun mapToJson(map: Map<*, *>): String = objectMapper.writeValueAsString(map)

    private fun <T, U> jsonToMap(json: String): Map<T, U> = objectMapper.readValue(json)

    @Suppress("CyclomaticComplexMethod")
    fun ResultRow.toRegisteredClient(): SpringRegisteredClient {
        fun resolveClientAuthenticationMethods(string: String): ClientAuthenticationMethod {
            return when (string) {
                ClientAuthenticationMethod.CLIENT_SECRET_BASIC.value -> ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                ClientAuthenticationMethod.CLIENT_SECRET_JWT.value -> ClientAuthenticationMethod.CLIENT_SECRET_JWT
                ClientAuthenticationMethod.CLIENT_SECRET_POST.value -> ClientAuthenticationMethod.CLIENT_SECRET_POST
                ClientAuthenticationMethod.NONE.value -> ClientAuthenticationMethod.NONE
                else -> {
                    ClientAuthenticationMethod(string)
                }
            }
        }

        fun resolveAuthorizationGrantType(string: String): AuthorizationGrantType {
            return when (string) {
                AuthorizationGrantType.AUTHORIZATION_CODE.value -> AuthorizationGrantType.AUTHORIZATION_CODE
                AuthorizationGrantType.CLIENT_CREDENTIALS.value -> AuthorizationGrantType.CLIENT_CREDENTIALS
                AuthorizationGrantType.REFRESH_TOKEN.value -> AuthorizationGrantType.REFRESH_TOKEN
                else -> {
                    AuthorizationGrantType(string)
                }
            }
        }

        val clientAuthenticationMethods = this[RegisteredClient.clientAuthenticationMethods].split(",").toSet()
        val authorizationGrantTypes = this[RegisteredClient.authorizationGrantTypes].split(",").toSet()
        val redirectUris = this[RegisteredClient.redirectUris]?.split(",").orEmpty().toSet()
        val postLogoutRedirectUris = this[RegisteredClient.postLogoutRedirectUris]?.split(",").orEmpty().toSet()
        val clientScopes = this[RegisteredClient.scopes].split(",").toSet()

        val builder = SpringRegisteredClient
            .withId(this[RegisteredClient.id])
            .clientId(this[clientId])
            .clientIdIssuedAt(this[RegisteredClient.clientIdIssuedAt])
            .clientSecret(this[RegisteredClient.clientSecret])
            .clientSecretExpiresAt(this[RegisteredClient.clientSecretExpiresAt])
            .clientName(this[RegisteredClient.clientName])
            .clientAuthenticationMethods {
                clientAuthenticationMethods.forEach { s ->
                    it.add(resolveClientAuthenticationMethods(s))
                }
            }
            .authorizationGrantTypes {
                authorizationGrantTypes.forEach { s ->
                    it.add(resolveAuthorizationGrantType(s))
                }
            }
            .redirectUris { it.addAll(redirectUris) }
            .postLogoutRedirectUris { it.addAll(postLogoutRedirectUris) }
            .scopes { it.addAll(clientScopes) }
            .clientSettings(ClientSettings.withSettings(jsonToMap(this[clientSettings])).build())

        val tokenSettingsMap = jsonToMap<String, Any>(this[tokenSettings])
        val withSettings = TokenSettings.withSettings(tokenSettingsMap)
        if (tokenSettingsMap.containsKey(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT)) {
            withSettings.accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
        }
        builder.tokenSettings(withSettings.build())

        return builder.build()
    }

    companion object {
        val objectMapper: ObjectMapper = ObjectMapper()
        val LOGGER: Logger = LoggerFactory.getLogger(RegisteredClientRepositoryImpl::class.java)

        init {

            val classLoader = ExposedOAuth2AuthorizationService::class.java.classLoader
            val modules = SecurityJackson2Modules.getModules(classLoader)
            objectMapper.registerModules(JavaTimeModule())
            objectMapper.registerModules(modules)
            objectMapper.registerModules(OAuth2AuthorizationServerJackson2Module())
        }
    }
}

// org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql
object RegisteredClient : Table("registered_client") {
    val id: Column<String> = varchar("id", 100)
    val clientId: Column<String> = varchar("client_id", 100)
    val clientIdIssuedAt: Column<Instant> = timestamp("client_id_issued_at").defaultExpression(CurrentTimestamp)
    val clientSecret: Column<String?> = varchar("client_secret", 200).nullable().default(null)
    val clientSecretExpiresAt: Column<Instant?> = timestamp("client_secret_expires_at").nullable().default(null)
    val clientName: Column<String> = varchar("client_name", 200)
    val clientAuthenticationMethods: Column<String> = varchar("client_authentication_methods", 1000)
    val authorizationGrantTypes: Column<String> = varchar("authorization_grant_types", 1000)
    val redirectUris: Column<String?> = varchar("redirect_uris", 1000).nullable().default(null)
    val postLogoutRedirectUris: Column<String?> = varchar("post_logout_redirect_uris", 1000).nullable().default(null)
    val scopes: Column<String> = varchar("scopes", 1000)
    val clientSettings: Column<String> = varchar("client_settings", 2000)
    val tokenSettings: Column<String> = varchar("token_settings", 2000)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
