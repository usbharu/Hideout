package dev.usbharu.hideout.routing.api.internal.v1

import dev.usbharu.hideout.domain.model.hideout.form.RefreshToken
import dev.usbharu.hideout.domain.model.hideout.form.UserLogin
import dev.usbharu.hideout.plugins.TOKEN_AUTH
import dev.usbharu.hideout.service.api.UserAuthApiService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@Deprecated("Ktor is deprecated")
fun Route.auth(userAuthApiService: UserAuthApiService) {
    post("/login") {
        val loginUser = call.receive<UserLogin>()
        return@post call.respond(userAuthApiService.login(loginUser.username, loginUser.password))
    }

    post("/refresh-token") {
        val refreshToken = call.receive<RefreshToken>()
        return@post call.respond(userAuthApiService.refreshToken(refreshToken))
    }
    authenticate(TOKEN_AUTH) {
        get("/auth-check") {
            val principal = call.principal<JWTPrincipal>() ?: throw IllegalStateException("no principal")
            val username = principal.payload.getClaim("uid")
            call.respondText("Hello $username")
        }
    }
}
