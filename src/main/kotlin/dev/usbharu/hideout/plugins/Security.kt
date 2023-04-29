@file:Suppress("UnusedPrivateMember")

package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.service.IUserAuthService
import io.ktor.server.application.*
import io.ktor.server.auth.*

const val TOKEN_AUTH = "token-auth"

fun Application.configureSecurity(userAuthService: IUserAuthService) {
    install(Authentication) {
        bearer(TOKEN_AUTH) {
            authenticate { bearerTokenCredential ->
                UserIdPrincipal(bearerTokenCredential.token)
            }
            skipWhen { true }
        }
    }
//    install(Sessions) {
//        cookie<UserSession>("MY_SESSION") {
//            cookie.extensions["SameSite"] = "lax"
//        }
//    }
}
