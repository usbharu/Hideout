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

import dev.usbharu.hideout.core.application.shared.Transaction
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.stereotype.Service
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent as AuthorizationConsent

@Service
class ExposedOAuth2AuthorizationConsentService(
    private val registeredClientRepository: RegisteredClientRepository,
    private val transaction: Transaction,
) :
    OAuth2AuthorizationConsentService {

    override fun save(authorizationConsent: AuthorizationConsent?): Unit = runBlocking {
        requireNotNull(authorizationConsent)
        transaction.transaction {
            val singleOrNull =
                OAuth2AuthorizationConsent.selectAll().where {
                    OAuth2AuthorizationConsent.registeredClientId
                        .eq(authorizationConsent.registeredClientId)
                        .and(OAuth2AuthorizationConsent.principalName.eq(authorizationConsent.principalName))
                }
                    .singleOrNull()
            if (singleOrNull == null) {
                OAuth2AuthorizationConsent.insert {
                    it[registeredClientId] = authorizationConsent.registeredClientId
                    it[principalName] = authorizationConsent.principalName
                    it[authorities] = authorizationConsent.authorities.joinToString(",")
                }
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

    override fun findById(registeredClientId: String?, principalName: String?): AuthorizationConsent? = runBlocking {
        requireNotNull(registeredClientId)
        requireNotNull(principalName)
        transaction.transaction {
            OAuth2AuthorizationConsent.selectAll().where {
                (OAuth2AuthorizationConsent.registeredClientId eq registeredClientId)
                    .and(OAuth2AuthorizationConsent.principalName eq principalName)
            }
                .singleOrNull()?.toAuthorizationConsent()
        }
    }

    fun ResultRow.toAuthorizationConsent(): AuthorizationConsent {
        val registeredClientId = this[OAuth2AuthorizationConsent.registeredClientId]
        registeredClientRepository.findById(registeredClientId)

        val principalName = this[OAuth2AuthorizationConsent.principalName]
        val builder = AuthorizationConsent.withId(registeredClientId, principalName)

        this[OAuth2AuthorizationConsent.authorities].split(",").forEach {
            builder.authority(SimpleGrantedAuthority(it))
        }

        return builder.build()
    }
}

object OAuth2AuthorizationConsent : Table("oauth2_authorization_consent") {
    val registeredClientId: Column<String> = varchar("registered_client_id", 100)
    val principalName: Column<String> = varchar("principal_name", 200)
    val authorities: Column<String> = varchar("authorities", 1000)
    override val primaryKey: PrimaryKey = PrimaryKey(registeredClientId, principalName)
}
