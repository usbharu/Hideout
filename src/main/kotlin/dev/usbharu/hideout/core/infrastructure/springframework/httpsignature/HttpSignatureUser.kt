package dev.usbharu.hideout.core.infrastructure.springframework.httpsignature

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import java.io.Serial

class HttpSignatureUser(
    username: String,
    val domain: String,
    val id: Long,
    credentialsNonExpired: Boolean,
    accountNonLocked: Boolean,
    authorities: MutableCollection<out GrantedAuthority>?
) : User(
    username,
    "",
    true,
    true,
    credentialsNonExpired,
    accountNonLocked,
    authorities
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HttpSignatureUser) return false
        if (!super.equals(other)) return false

        if (domain != other.domain) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + domain.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "HttpSignatureUser(" +
                "domain='$domain', " +
                "id=$id" +
                ")" +
                " ${super.toString()}"
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3330552099960982997L
    }
}
