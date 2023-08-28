package dev.usbharu.hideout.service.auth

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher


class UsernamePasswordAuthFilter(jwtService: JwtService, authenticationManager: AuthenticationManager?) :
    UsernamePasswordAuthenticationFilter(authenticationManager) {
    init {
        setRequiresAuthenticationRequestMatcher(AntPathRequestMatcher("/api/internal/v1/login", "POST"))

        this.setAuthenticationSuccessHandler { request, response, authentication ->

        }
    }
}
