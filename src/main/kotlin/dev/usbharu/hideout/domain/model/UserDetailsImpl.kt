package dev.usbharu.hideout.domain.model

import org.springframework.security.core.GrantedAuthority
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
