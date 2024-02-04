package dev.usbharu.hideout.core.infrastructure.springframework.oauth2

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.application.external.Transaction
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
import java.time.Instant

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
            val singleOrNull = Authorization.selectAll().where { Authorization.id eq authorization.id }.singleOrNull()
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
        Authorization.deleteWhere { id eq authorization.id }
    }

    override fun findById(id: String?): OAuth2Authorization? {
        if (id == null) {
            return null
        }
        return Authorization.selectAll().where { Authorization.id eq id }.singleOrNull()?.toAuthorization()
    }

    override fun findByToken(token: String?, tokenType: OAuth2TokenType?): OAuth2Authorization? = runBlocking {
        requireNotNull(token)
        transaction.transaction {
            when (tokenType?.value) {
                null -> {
                    Authorization.selectAll().where { Authorization.authorizationCodeValue eq token }.orWhere {
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
                    Authorization.selectAll().where { Authorization.state eq token }
                }

                OAuth2ParameterNames.CODE -> {
                    Authorization.selectAll().where { Authorization.authorizationCodeValue eq token }
                }

                OAuth2ParameterNames.ACCESS_TOKEN -> {
                    Authorization.selectAll().where { Authorization.accessTokenValue eq token }
                }

                OidcParameterNames.ID_TOKEN -> {
                    Authorization.selectAll().where { Authorization.oidcIdTokenValue eq token }
                }

                OAuth2ParameterNames.REFRESH_TOKEN -> {
                    Authorization.selectAll().where { Authorization.refreshTokenValue eq token }
                }

                OAuth2ParameterNames.USER_CODE -> {
                    Authorization.selectAll().where { Authorization.userCodeValue eq token }
                }

                OAuth2ParameterNames.DEVICE_CODE -> {
                    Authorization.selectAll().where { Authorization.deviceCodeValue eq token }
                }

                else -> {
                    null
                }
            }?.singleOrNull()?.toAuthorization()
        }
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod", "CastToNullableType", "UNCHECKED_CAST")
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
                oidcTokenMetadata.getValue(OAuth2Authorization.Token.CLAIMS_METADATA_NAME)
                    as MutableMap<String, Any>?
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
            objectMapper.registerModules(JavaTimeModule())
            objectMapper.registerModules(modules)
            objectMapper.registerModules(OAuth2AuthorizationServerJackson2Module())
            objectMapper.registerModules(CoreJackson2Module())
            objectMapper.addMixIn(UserDetailsImpl::class.java, UserDetailsMixin::class.java)
        }
    }
}

object Authorization : Table("application_authorization") {
    val id: Column<String> = varchar("id", 255)
    val registeredClientId: Column<String> = varchar("registered_client_id", 255)
    val principalName: Column<String> = varchar("principal_name", 255)
    val authorizationGrantType: Column<String> = varchar("authorization_grant_type", 255)
    val authorizedScopes: Column<String?> = varchar("authorized_scopes", 1000).nullable().default(null)
    val attributes: Column<String?> = varchar("attributes", 4000).nullable().default(null)
    val state: Column<String?> = varchar("state", 500).nullable().default(null)
    val authorizationCodeValue: Column<String?> = varchar("authorization_code_value", 4000).nullable().default(null)
    val authorizationCodeIssuedAt: Column<Instant?> = timestamp("authorization_code_issued_at").nullable().default(null)
    val authorizationCodeExpiresAt: Column<Instant?> = timestamp("authorization_code_expires_at").nullable().default(
        null
    )
    val authorizationCodeMetadata: Column<String?> = varchar("authorization_code_metadata", 2000).nullable().default(
        null
    )
    val accessTokenValue: Column<String?> = varchar("access_token_value", 4000).nullable().default(null)
    val accessTokenIssuedAt: Column<Instant?> = timestamp("access_token_issued_at").nullable().default(null)
    val accessTokenExpiresAt: Column<Instant?> = timestamp("access_token_expires_at").nullable().default(null)
    val accessTokenMetadata: Column<String?> = varchar("access_token_metadata", 2000).nullable().default(null)
    val accessTokenType: Column<String?> = varchar("access_token_type", 255).nullable().default(null)
    val accessTokenScopes: Column<String?> = varchar("access_token_scopes", 1000).nullable().default(null)
    val refreshTokenValue: Column<String?> = varchar("refresh_token_value", 4000).nullable().default(null)
    val refreshTokenIssuedAt: Column<Instant?> = timestamp("refresh_token_issued_at").nullable().default(null)
    val refreshTokenExpiresAt: Column<Instant?> = timestamp("refresh_token_expires_at").nullable().default(null)
    val refreshTokenMetadata: Column<String?> = varchar("refresh_token_metadata", 2000).nullable().default(null)
    val oidcIdTokenValue: Column<String?> = varchar("oidc_id_token_value", 4000).nullable().default(null)
    val oidcIdTokenIssuedAt: Column<Instant?> = timestamp("oidc_id_token_issued_at").nullable().default(null)
    val oidcIdTokenExpiresAt: Column<Instant?> = timestamp("oidc_id_token_expires_at").nullable().default(null)
    val oidcIdTokenMetadata: Column<String?> = varchar("oidc_id_token_metadata", 2000).nullable().default(null)
    val oidcIdTokenClaims: Column<String?> = varchar("oidc_id_token_claims", 2000).nullable().default(null)
    val userCodeValue: Column<String?> = varchar("user_code_value", 4000).nullable().default(null)
    val userCodeIssuedAt: Column<Instant?> = timestamp("user_code_issued_at").nullable().default(null)
    val userCodeExpiresAt: Column<Instant?> = timestamp("user_code_expires_at").nullable().default(null)
    val userCodeMetadata: Column<String?> = varchar("user_code_metadata", 2000).nullable().default(null)
    val deviceCodeValue: Column<String?> = varchar("device_code_value", 4000).nullable().default(null)
    val deviceCodeIssuedAt: Column<Instant?> = timestamp("device_code_issued_at").nullable().default(null)
    val deviceCodeExpiresAt: Column<Instant?> = timestamp("device_code_expires_at").nullable().default(null)
    val deviceCodeMetadata: Column<String?> = varchar("device_code_metadata", 2000).nullable().default(null)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
