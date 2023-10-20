package dev.usbharu.hideout.service.signature

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import java.io.Serial

class HttpSignatureUser(
    username: String,
    val domain: String,
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
    companion object {
        @Serial
        private const val serialVersionUID: Long = -3330552099960982997L
    }
}
