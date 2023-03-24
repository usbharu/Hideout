package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.service.IUserAuthService
import dev.usbharu.hideout.service.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import kotlin.time.Duration.Companion.days

data class UserSession(val username: String) : Principal

const val tokenAuth = "token-auth"

fun Application.configureSecurity(userAuthService: IUserAuthService) {
    install(Authentication){
        bearer(tokenAuth) {
            authenticate {
                bearerTokenCredential ->
                UserIdPrincipal(bearerTokenCredential.token)
            }
            skipWhen { true }
        }
    }
}
