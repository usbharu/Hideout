package dev.usbharu.hideout.service.auth

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.stereotype.Service
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent as AuthorizationConsent

@Service
class ExposedOAuth2AuthorizationConsentService(private val registeredClientRepository: RegisteredClientRepository) :
    OAuth2AuthorizationConsentService {
    override fun save(authorizationConsent: AuthorizationConsent?) {
        requireNotNull(authorizationConsent)
        val singleOrNull =
            OAuth2AuthorizationConsent.select { OAuth2AuthorizationConsent.registeredClientId eq authorizationConsent.registeredClientId and (OAuth2AuthorizationConsent.principalName eq authorizationConsent.principalName) }
                .singleOrNull()
        if (singleOrNull == null) {
            OAuth2AuthorizationConsent.insert {
                it[registeredClientId] = authorizationConsent.registeredClientId
                it[principalName] = authorizationConsent.principalName
                it[authorities] = authorizationConsent.authorities.joinToString(",")
            }
        }
    }

    override fun remove(authorizationConsent: AuthorizationConsent?) {
        if (authorizationConsent == null) {
            return
        }
        OAuth2AuthorizationConsent.deleteWhere {
            registeredClientId eq authorizationConsent.registeredClientId and (principalName eq principalName)
        }
    }

    override fun findById(registeredClientId: String?, principalName: String?): AuthorizationConsent? {
        requireNotNull(registeredClientId)
        requireNotNull(principalName)

        return OAuth2AuthorizationConsent.select { OAuth2AuthorizationConsent.registeredClientId eq registeredClientId and (OAuth2AuthorizationConsent.principalName eq principalName) }
            .singleOrNull()?.toAuthorizationConsent()
    }

    fun ResultRow.toAuthorizationConsent(): AuthorizationConsent {
        val registeredClientId = this[OAuth2AuthorizationConsent.registeredClientId]
        val registeredClient = registeredClientRepository.findById(registeredClientId)

        val principalName = this[OAuth2AuthorizationConsent.principalName]
        val builder = AuthorizationConsent.withId(registeredClientId, principalName)

        this[OAuth2AuthorizationConsent.authorities].split(",").forEach {
            builder.authority(SimpleGrantedAuthority(it))
        }

        return builder.build()
    }
}

object OAuth2AuthorizationConsent : Table("oauth2_authorization_consent") {
    val registeredClientId = varchar("registered_client_id", 100)
    val principalName = varchar("principal_name", 200)
    val authorities = varchar("authorities", 1000)
    override val primaryKey = PrimaryKey(registeredClientId, principalName)
}