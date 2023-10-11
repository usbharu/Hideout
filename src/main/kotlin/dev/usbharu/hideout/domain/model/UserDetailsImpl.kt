package dev.usbharu.hideout.domain.model

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

    private val SIMPLE_GRANTED_AUTHORITY_SET = object : TypeReference<Set<SimpleGrantedAuthority>>() {}
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
}
