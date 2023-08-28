package dev.usbharu.hideout.repository

import dev.usbharu.hideout.repository.RegisteredClient.clientId
import dev.usbharu.hideout.repository.RegisteredClient.clientSettings
import dev.usbharu.hideout.repository.RegisteredClient.tokenSettings
import dev.usbharu.hideout.util.JsonUtil
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.stereotype.Repository
import java.time.Instant
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient as SpringRegisteredClient

@Repository
class RegisteredClientRepositoryImpl(private val database: Database) : RegisteredClientRepository {

    init {
        transaction(database) {
            SchemaUtils.create(RegisteredClient)
            SchemaUtils.createMissingTablesAndColumns(RegisteredClient)
        }
    }

    override fun save(registeredClient: SpringRegisteredClient?) {
        requireNotNull(registeredClient)
        val singleOrNull = RegisteredClient.select { RegisteredClient.id eq registeredClient.id }.singleOrNull()
        if (singleOrNull == null) {
            RegisteredClient.insert {
                it[id] = registeredClient.id
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
                it[clientSettings] = JsonUtil.mapToJson(registeredClient.clientSettings.settings)
                it[tokenSettings] = JsonUtil.mapToJson(registeredClient.tokenSettings.settings)
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
                it[clientSettings] = JsonUtil.mapToJson(registeredClient.clientSettings.settings)
                it[tokenSettings] = JsonUtil.mapToJson(registeredClient.tokenSettings.settings)
            }
        }
    }

    override fun findById(id: String?): SpringRegisteredClient? {
        if (id == null) {
            return null
        }
        return RegisteredClient.select {
            RegisteredClient.id eq id
        }.singleOrNull()?.toRegisteredClient()
    }

    override fun findByClientId(clientId: String?): SpringRegisteredClient? {
        if (clientId == null) {
            return null
        }
        return RegisteredClient.select {
            RegisteredClient.clientId eq clientId
        }.singleOrNull()?.toRegisteredClient()
    }
}


// org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql
object RegisteredClient : Table("registered_client") {
    val id = varchar("id", 100)
    val clientId = varchar("client_id", 100)
    val clientIdIssuedAt = timestamp("client_id_issued_at").defaultExpression(CurrentTimestamp())
    val clientSecret = varchar("client_secret", 200).nullable().default(null)
    val clientSecretExpiresAt = timestamp("client_secret_expires_at").nullable().default(null)
    val clientName = varchar("client_name", 200)
    val clientAuthenticationMethods = varchar("client_authentication_methods", 1000)
    val authorizationGrantTypes = varchar("authorization_grant_types", 1000)
    val redirectUris = varchar("redirect_uris", 1000).nullable().default(null)
    val postLogoutRedirectUris = varchar("post_logout_redirect_uris", 1000).nullable().default(null)
    val scopes = varchar("scopes", 1000)
    val clientSettings = varchar("client_settings", 2000)
    val tokenSettings = varchar("token_settings", 2000)

    override val primaryKey = PrimaryKey(id)
}

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
        .clientSettings(ClientSettings.withSettings(JsonUtil.jsonToMap(this[clientSettings])).build())


    val tokenSettingsMap = JsonUtil.jsonToMap<String, Any>(this[tokenSettings])
    val withSettings = TokenSettings.withSettings(tokenSettingsMap)
    if (tokenSettingsMap.containsKey(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT)) {
        withSettings.accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
    }
    builder.tokenSettings(withSettings.build())

    return builder.build()
}
