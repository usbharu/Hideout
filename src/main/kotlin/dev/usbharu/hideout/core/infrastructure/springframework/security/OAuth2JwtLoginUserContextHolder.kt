package dev.usbharu.hideout.core.infrastructure.springframework.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class OAuth2JwtLoginUserContextHolder : LoginUserContextHolder {
    override fun getLoginUserId(): Long {
        val principal = SecurityContextHolder.getContext().getAuthentication().principal as Jwt

        return principal.getClaim<String>("uid").toLong()
    }

    override fun getLoginUserIdOrNull(): Long? {
        val principal = SecurityContextHolder.getContext()?.authentication?.principal
        if (principal !is Jwt) {
            return null
        }

        return principal.getClaim<String>("uid").toLongOrNull()
    }
}
