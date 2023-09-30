package dev.usbharu.hideout.service.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.domain.model.UserDetailsImpl
import dev.usbharu.hideout.domain.model.UserDetailsMixin
import dev.usbharu.hideout.service.core.Transaction
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.springframework.security.jackson2.CoreJackson2Module
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.core.*
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.stereotype.Service

@Service
class ExposedOAuth2AuthorizationService(
    private val registeredClientRepository: RegisteredClientRepository,
    private val transaction: Transaction,
) :
    OAuth2AuthorizationService {

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    override fun save(authorization: OAuth2Authorization?): Unit = runBlocking {
        requireNotNull(authorization)
        transaction.transaction {
            val singleOrNull = Authorization.select { Authorization.id eq authorization.id }.singleOrNull()
            if (singleOrNull == null) {
                val authorizationCodeToken = authorization.getToken(OAuth2AuthorizationCode::class.java)
                val accessToken = authorization.getToken(OAuth2AccessToken::class.java)
                val refreshToken = authorization.getToken(OAuth2RefreshToken::class.java)
                val oidcIdToken = authorization.getToken(OidcIdToken::class.java)
                val userCode = authorization.getToken(OAuth2UserCode::class.java)
                val deviceCode = authorization.getToken(OAuth2DeviceCode::class.java)
                Authorization.insert {
                    it[id] = authorization.id
                    it[registeredClientId] = authorization.registeredClientId
                    it[principalName] = authorization.principalName
                    it[authorizationGrantType] = authorization.authorizationGrantType.value
                    it[authorizedScopes] =
                        authorization.authorizedScopes.joinToString(",").takeIf { s -> s.isNotEmpty() }
                    it[attributes] = mapToJson(authorization.attributes)
                    it[state] = authorization.getAttribute(OAuth2ParameterNames.STATE)
                    it[authorizationCodeValue] = authorizationCodeToken?.token?.tokenValue
                    it[authorizationCodeIssuedAt] = authorizationCodeToken?.token?.issuedAt
                    it[authorizationCodeExpiresAt] = authorizationCodeToken?.token?.expiresAt
                    it[authorizationCodeMetadata] =
                        authorizationCodeToken?.metadata?.let { it1 -> mapToJson(it1) }
                    it[accessTokenValue] = accessToken?.token?.tokenValue
                    it[accessTokenIssuedAt] = accessToken?.token?.issuedAt
                    it[accessTokenExpiresAt] = accessToken?.token?.expiresAt
                    it[accessTokenMetadata] = accessToken?.metadata?.let { it1 -> mapToJson(it1) }
                    it[accessTokenType] = accessToken?.token?.tokenType?.value
                    it[accessTokenScopes] =
                        accessToken?.run { token.scopes.joinToString(",").takeIf { it.isNotEmpty() } }
                    it[refreshTokenValue] = refreshToken?.token?.tokenValue
                    it[refreshTokenIssuedAt] = refreshToken?.token?.issuedAt
                    it[refreshTokenExpiresAt] = refreshToken?.token?.expiresAt
                    it[refreshTokenMetadata] = refreshToken?.metadata?.let { it1 -> mapToJson(it1) }
                    it[oidcIdTokenValue] = oidcIdToken?.token?.tokenValue
                    it[oidcIdTokenIssuedAt] = oidcIdToken?.token?.issuedAt
                    it[oidcIdTokenExpiresAt] = oidcIdToken?.token?.expiresAt
                    it[oidcIdTokenMetadata] = oidcIdToken?.metadata?.let { it1 -> mapToJson(it1) }
                    it[userCodeValue] = userCode?.token?.tokenValue
                    it[userCodeIssuedAt] = userCode?.token?.issuedAt
                    it[userCodeExpiresAt] = userCode?.token?.expiresAt
                    it[userCodeMetadata] = userCode?.metadata?.let { it1 -> mapToJson(it1) }
                    it[deviceCodeValue] = deviceCode?.token?.tokenValue
                    it[deviceCodeIssuedAt] = deviceCode?.token?.issuedAt
                    it[deviceCodeExpiresAt] = deviceCode?.token?.expiresAt
                    it[deviceCodeMetadata] = deviceCode?.metadata?.let { it1 -> mapToJson(it1) }
                }
            } else {
                val authorizationCodeToken = authorization.getToken(OAuth2AuthorizationCode::class.java)
                val accessToken = authorization.getToken(OAuth2AccessToken::class.java)
                val refreshToken = authorization.getToken(OAuth2RefreshToken::class.java)
                val oidcIdToken = authorization.getToken(OidcIdToken::class.java)
                val userCode = authorization.getToken(OAuth2UserCode::class.java)
                val deviceCode = authorization.getToken(OAuth2DeviceCode::class.java)
                Authorization.update({ Authorization.id eq authorization.id }) {
                    it[registeredClientId] = authorization.registeredClientId
                    it[principalName] = authorization.principalName
                    it[authorizationGrantType] = authorization.authorizationGrantType.value
                    it[authorizedScopes] =
                        authorization.authorizedScopes.joinToString(",").takeIf { s -> s.isNotEmpty() }
                    it[attributes] = mapToJson(authorization.attributes)
                    it[state] = authorization.getAttribute(OAuth2ParameterNames.STATE)
                    it[authorizationCodeValue] = authorizationCodeToken?.token?.tokenValue
                    it[authorizationCodeIssuedAt] = authorizationCodeToken?.token?.issuedAt
                    it[authorizationCodeExpiresAt] = authorizationCodeToken?.token?.expiresAt
                    it[authorizationCodeMetadata] =
                        authorizationCodeToken?.metadata?.let { it1 -> mapToJson(it1) }
                    it[accessTokenValue] = accessToken?.token?.tokenValue
                    it[accessTokenIssuedAt] = accessToken?.token?.issuedAt
                    it[accessTokenExpiresAt] = accessToken?.token?.expiresAt
                    it[accessTokenMetadata] = accessToken?.metadata?.let { it1 -> mapToJson(it1) }
                    it[accessTokenType] = accessToken?.run { token.tokenType.value }
                    it[accessTokenScopes] =
                        accessToken?.run { token.scopes.joinToString(",").takeIf { s -> s.isNotEmpty() } }
                    it[refreshTokenValue] = refreshToken?.token?.tokenValue
                    it[refreshTokenIssuedAt] = refreshToken?.token?.issuedAt
                    it[refreshTokenExpiresAt] = refreshToken?.token?.expiresAt
                    it[refreshTokenMetadata] = refreshToken?.metadata?.let { it1 -> mapToJson(it1) }
                    it[oidcIdTokenValue] = oidcIdToken?.token?.tokenValue
                    it[oidcIdTokenIssuedAt] = oidcIdToken?.token?.issuedAt
                    it[oidcIdTokenExpiresAt] = oidcIdToken?.token?.expiresAt
                    it[oidcIdTokenMetadata] = oidcIdToken?.metadata?.let { it1 -> mapToJson(it1) }
                    it[userCodeValue] = userCode?.token?.tokenValue
                    it[userCodeIssuedAt] = userCode?.token?.issuedAt
                    it[userCodeExpiresAt] = userCode?.token?.expiresAt
                    it[userCodeMetadata] = userCode?.metadata?.let { it1 -> mapToJson(it1) }
                    it[deviceCodeValue] = deviceCode?.token?.tokenValue
                    it[deviceCodeIssuedAt] = deviceCode?.token?.issuedAt
                    it[deviceCodeExpiresAt] = deviceCode?.token?.expiresAt
                    it[deviceCodeMetadata] = deviceCode?.metadata?.let { it1 -> mapToJson(it1) }
                }
            }
        }
    }

    override fun remove(authorization: OAuth2Authorization?) {
        if (authorization == null) {
            return
        }
        Authorization.deleteWhere { Authorization.id eq authorization.id }
    }

    override fun findById(id: String?): OAuth2Authorization? {
        if (id == null) {
            return null
        }
        return Authorization.select { Authorization.id eq id }.singleOrNull()?.toAuthorization()
    }

    override fun findByToken(token: String?, tokenType: OAuth2TokenType?): OAuth2Authorization? = runBlocking {
        requireNotNull(token)
        transaction.transaction {
            when (tokenType?.value) {
                null -> {
                    Authorization.select {
                        Authorization.authorizationCodeValue eq token
                    }.orWhere {
                        Authorization.accessTokenValue eq token
                    }.orWhere {
                        Authorization.oidcIdTokenValue eq token
                    }.orWhere {
                        Authorization.refreshTokenValue eq token
                    }.orWhere {
                        Authorization.userCodeValue eq token
                    }.orWhere {
                        Authorization.deviceCodeValue eq token
                    }
                }

                OAuth2ParameterNames.STATE -> {
                    Authorization.select { Authorization.state eq token }
                }

                OAuth2ParameterNames.CODE -> {
                    Authorization.select { Authorization.authorizationCodeValue eq token }
                }

                OAuth2ParameterNames.ACCESS_TOKEN -> {
                    Authorization.select { Authorization.accessTokenValue eq token }
                }

                OidcParameterNames.ID_TOKEN -> {
                    Authorization.select { Authorization.oidcIdTokenValue eq token }
                }

                OAuth2ParameterNames.REFRESH_TOKEN -> {
                    Authorization.select { Authorization.refreshTokenValue eq token }
                }

                OAuth2ParameterNames.USER_CODE -> {
                    Authorization.select { Authorization.userCodeValue eq token }
                }

                OAuth2ParameterNames.DEVICE_CODE -> {
                    Authorization.select { Authorization.deviceCodeValue eq token }
                }

                else -> {
                    null
                }
            }?.singleOrNull()?.toAuthorization()
        }
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    fun ResultRow.toAuthorization(): OAuth2Authorization {
        val registeredClientId = this[Authorization.registeredClientId]

        val registeredClient = registeredClientRepository.findById(registeredClientId)

        val builder = OAuth2Authorization.withRegisteredClient(registeredClient)
        val id = this[Authorization.id]
        val principalName = this[Authorization.principalName]
        val authorizationGrantType = this[Authorization.authorizationGrantType]
        val authorizedScopes = this[Authorization.authorizedScopes]?.split(",").orEmpty().toSet()
        val attributes = this[Authorization.attributes]?.let { jsonToMap<String, Any>(it) }.orEmpty()

        builder.id(id).principalName(principalName)
            .authorizationGrantType(AuthorizationGrantType(authorizationGrantType)).authorizedScopes(authorizedScopes)
            .attributes { it.putAll(attributes) }

        val state = this[Authorization.state].orEmpty()
        if (state.isNotBlank()) {
            builder.attribute(OAuth2ParameterNames.STATE, state)
        }

        val authorizationCodeValue = this[Authorization.authorizationCodeValue].orEmpty()
        if (authorizationCodeValue.isNotBlank()) {
            val authorizationCodeIssuedAt = this[Authorization.authorizationCodeIssuedAt]
            val authorizationCodeExpiresAt = this[Authorization.authorizationCodeExpiresAt]
            val authorizationCodeMetadata = this[Authorization.authorizationCodeMetadata]?.let {
                jsonToMap<String, Any>(
                    it
                )
            }.orEmpty()
            val oAuth2AuthorizationCode =
                OAuth2AuthorizationCode(authorizationCodeValue, authorizationCodeIssuedAt, authorizationCodeExpiresAt)
            builder.token(oAuth2AuthorizationCode) {
                it.putAll(authorizationCodeMetadata)
            }
        }

        val accessTokenValue = this[Authorization.accessTokenValue].orEmpty()
        if (accessTokenValue.isNotBlank()) {
            val accessTokenIssuedAt = this[Authorization.accessTokenIssuedAt]
            val accessTokenExpiresAt = this[Authorization.accessTokenExpiresAt]
            val accessTokenMetadata =
                this[Authorization.accessTokenMetadata]?.let { jsonToMap<String, Any>(it) }.orEmpty()
            val accessTokenType =
                if (this[Authorization.accessTokenType].equals(OAuth2AccessToken.TokenType.BEARER.value, true)) {
                    OAuth2AccessToken.TokenType.BEARER
                } else {
                    null
                }

            val accessTokenScope = this[Authorization.accessTokenScopes]?.split(",").orEmpty().toSet()

            val oAuth2AccessToken = OAuth2AccessToken(
                accessTokenType,
                accessTokenValue,
                accessTokenIssuedAt,
                accessTokenExpiresAt,
                accessTokenScope
            )

            builder.token(oAuth2AccessToken) { it.putAll(accessTokenMetadata) }
        }

        val oidcIdTokenValue = this[Authorization.oidcIdTokenValue].orEmpty()
        if (oidcIdTokenValue.isNotBlank()) {
            val oidcTokenIssuedAt = this[Authorization.oidcIdTokenIssuedAt]
            val oidcTokenExpiresAt = this[Authorization.oidcIdTokenExpiresAt]
            val oidcTokenMetadata =
                this[Authorization.oidcIdTokenMetadata]?.let { jsonToMap<String, Any>(it) }.orEmpty()

            val oidcIdToken = OidcIdToken(
                oidcIdTokenValue,
                oidcTokenIssuedAt,
                oidcTokenExpiresAt,
                oidcTokenMetadata.getValue(OAuth2Authorization.Token.CLAIMS_METADATA_NAME) as MutableMap<String, Any>?
            )

            builder.token(oidcIdToken) { it.putAll(oidcTokenMetadata) }
        }

        val refreshTokenValue = this[Authorization.refreshTokenValue].orEmpty()
        if (refreshTokenValue.isNotBlank()) {
            val refreshTokenIssuedAt = this[Authorization.refreshTokenIssuedAt]
            val refreshTokenExpiresAt = this[Authorization.refreshTokenExpiresAt]
            val refreshTokenMetadata =
                this[Authorization.refreshTokenMetadata]?.let { jsonToMap<String, Any>(it) }.orEmpty()

            val oAuth2RefreshToken = OAuth2RefreshToken(refreshTokenValue, refreshTokenIssuedAt, refreshTokenExpiresAt)

            builder.token(oAuth2RefreshToken) { it.putAll(refreshTokenMetadata) }
        }

        val userCodeValue = this[Authorization.userCodeValue].orEmpty()
        if (userCodeValue.isNotBlank()) {
            val userCodeIssuedAt = this[Authorization.userCodeIssuedAt]
            val userCodeExpiresAt = this[Authorization.userCodeExpiresAt]
            val userCodeMetadata =
                this[Authorization.userCodeMetadata]?.let { jsonToMap<String, Any>(it) }.orEmpty()
            val oAuth2UserCode = OAuth2UserCode(userCodeValue, userCodeIssuedAt, userCodeExpiresAt)
            builder.token(oAuth2UserCode) { it.putAll(userCodeMetadata) }
        }

        val deviceCodeValue = this[Authorization.deviceCodeValue].orEmpty()
        if (deviceCodeValue.isNotBlank()) {
            val deviceCodeIssuedAt = this[Authorization.deviceCodeIssuedAt]
            val deviceCodeExpiresAt = this[Authorization.deviceCodeExpiresAt]
            val deviceCodeMetadata =
                this[Authorization.deviceCodeMetadata]?.let { jsonToMap<String, Any>(it) }.orEmpty()

            val oAuth2DeviceCode = OAuth2DeviceCode(deviceCodeValue, deviceCodeIssuedAt, deviceCodeExpiresAt)
            builder.token(oAuth2DeviceCode) { it.putAll(deviceCodeMetadata) }
        }

        return builder.build()
    }

    private fun mapToJson(map: Map<*, *>): String = objectMapper.writeValueAsString(map)

    private fun <T, U> jsonToMap(json: String): Map<T, U> = objectMapper.readValue(json)

    companion object {
        val objectMapper: ObjectMapper = ObjectMapper()

        init {

            val classLoader = ExposedOAuth2AuthorizationService::class.java.classLoader
            val modules = SecurityJackson2Modules.getModules(classLoader)
            this.objectMapper.registerModules(JavaTimeModule())
            this.objectMapper.registerModules(modules)
            this.objectMapper.registerModules(OAuth2AuthorizationServerJackson2Module())
            this.objectMapper.registerModules(CoreJackson2Module())
            this.objectMapper.addMixIn(UserDetailsImpl::class.java, UserDetailsMixin::class.java)
        }
    }
}

object Authorization : Table("application_authorization") {
    val id = varchar("id", 255)
    val registeredClientId = varchar("registered_client_id", 255)
    val principalName = varchar("principal_name", 255)
    val authorizationGrantType = varchar("authorization_grant_type", 255)
    val authorizedScopes = varchar("authorized_scopes", 1000).nullable().default(null)
    val attributes = varchar("attributes", 4000).nullable().default(null)
    val state = varchar("state", 500).nullable().default(null)
    val authorizationCodeValue = varchar("authorization_code_value", 4000).nullable().default(null)
    val authorizationCodeIssuedAt = timestamp("authorization_code_issued_at").nullable().default(null)
    val authorizationCodeExpiresAt = timestamp("authorization_code_expires_at").nullable().default(null)
    val authorizationCodeMetadata = varchar("authorization_code_metadata", 2000).nullable().default(null)
    val accessTokenValue = varchar("access_token_value", 4000).nullable().default(null)
    val accessTokenIssuedAt = timestamp("access_token_issued_at").nullable().default(null)
    val accessTokenExpiresAt = timestamp("access_token_expires_at").nullable().default(null)
    val accessTokenMetadata = varchar("access_token_metadata", 2000).nullable().default(null)
    val accessTokenType = varchar("access_token_type", 255).nullable().default(null)
    val accessTokenScopes = varchar("access_token_scopes", 1000).nullable().default(null)
    val refreshTokenValue = varchar("refresh_token_value", 4000).nullable().default(null)
    val refreshTokenIssuedAt = timestamp("refresh_token_issued_at").nullable().default(null)
    val refreshTokenExpiresAt = timestamp("refresh_token_expires_at").nullable().default(null)
    val refreshTokenMetadata = varchar("refresh_token_metadata", 2000).nullable().default(null)
    val oidcIdTokenValue = varchar("oidc_id_token_value", 4000).nullable().default(null)
    val oidcIdTokenIssuedAt = timestamp("oidc_id_token_issued_at").nullable().default(null)
    val oidcIdTokenExpiresAt = timestamp("oidc_id_token_expires_at").nullable().default(null)
    val oidcIdTokenMetadata = varchar("oidc_id_token_metadata", 2000).nullable().default(null)
    val oidcIdTokenClaims = varchar("oidc_id_token_claims", 2000).nullable().default(null)
    val userCodeValue = varchar("user_code_value", 4000).nullable().default(null)
    val userCodeIssuedAt = timestamp("user_code_issued_at").nullable().default(null)
    val userCodeExpiresAt = timestamp("user_code_expires_at").nullable().default(null)
    val userCodeMetadata = varchar("user_code_metadata", 2000).nullable().default(null)
    val deviceCodeValue = varchar("device_code_value", 4000).nullable().default(null)
    val deviceCodeIssuedAt = timestamp("device_code_issued_at").nullable().default(null)
    val deviceCodeExpiresAt = timestamp("device_code_expires_at").nullable().default(null)
    val deviceCodeMetadata = varchar("device_code_metadata", 2000).nullable().default(null)

    override val primaryKey = PrimaryKey(id)
}
