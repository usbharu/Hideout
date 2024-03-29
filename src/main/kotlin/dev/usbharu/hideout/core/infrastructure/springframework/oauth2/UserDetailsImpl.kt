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

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import java.io.Serial

class UserDetailsImpl(
    val id: Long,
    username: String?,
    password: String?,
    enabled: Boolean,
    accountNonExpired: Boolean,
    credentialsNonExpired: Boolean,
    accountNonLocked: Boolean,
    authorities: MutableCollection<out GrantedAuthority>?
) : User(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities) {
    override fun toString(): String {
        return "UserDetailsImpl(" +
            "id=$id" +
            ")" +
            " ${super.toString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UserDetailsImpl

        return id == other.id
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -899168205656607781L
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonDeserialize(using = UserDetailsDeserializer::class)
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSubTypes
@Suppress("UnnecessaryAbstractClass")
abstract class UserDetailsMixin

class UserDetailsDeserializer : JsonDeserializer<UserDetailsImpl>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UserDetailsImpl {
        val mapper = p.codec as ObjectMapper
        val jsonNode: JsonNode = mapper.readTree(p)
        val authorities: Set<GrantedAuthority> = mapper.convertValue(
            jsonNode["authorities"],
            SIMPLE_GRANTED_AUTHORITY_SET
        )

        val password = jsonNode.readText("password")
        return UserDetailsImpl(
            id = jsonNode["id"].longValue(),
            username = jsonNode.readText("username"),
            password = password,
            enabled = true,
            accountNonExpired = true,
            credentialsNonExpired = true,
            accountNonLocked = true,
            authorities = authorities.toMutableList(),
        )
    }

    fun JsonNode.readText(field: String, defaultValue: String = ""): String {
        return when {
            has(field) -> get(field).asText(defaultValue)
            else -> defaultValue
        }
    }

    companion object {
        private val SIMPLE_GRANTED_AUTHORITY_SET = object : TypeReference<Set<SimpleGrantedAuthority>>() {}
    }
}
