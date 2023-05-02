package dev.usbharu.hideout.routing

import dev.usbharu.hideout.plugins.TOKEN_AUTH
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.authTestRouting() {
    authenticate(TOKEN_AUTH) {
        get("/auth-check") {
            val principal = call.principal<JWTPrincipal>()
            val username = principal!!.payload.getClaim("username")
            call.respondText("Hello $username")
        }
    }
}
