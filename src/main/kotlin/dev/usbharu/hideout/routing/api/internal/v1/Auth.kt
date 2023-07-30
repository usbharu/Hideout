package dev.usbharu.hideout.routing.api.internal.v1

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.form.RefreshToken
import dev.usbharu.hideout.domain.model.hideout.form.UserLogin
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.plugins.TOKEN_AUTH
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.auth.IJwtService
import dev.usbharu.hideout.service.user.IUserAuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.auth(
    userAuthService: IUserAuthService,
    userRepository: IUserRepository,
    jwtService: IJwtService
) {
    post("/login") {
        val loginUser = call.receive<UserLogin>()
        val check = userAuthService.verifyAccount(loginUser.username, loginUser.password)
        if (check.not()) {
            return@post call.respond(HttpStatusCode.Unauthorized)
        }

        val user = userRepository.findByNameAndDomain(loginUser.username, Config.configData.domain)
            ?: throw UserNotFoundException("${loginUser.username} was not found.")

        return@post call.respond(jwtService.createToken(user))
    }

    post("/refresh-token") {
        val refreshToken = call.receive<RefreshToken>()
        return@post call.respond(jwtService.refreshToken(refreshToken))
    }
    authenticate(TOKEN_AUTH) {
        get("/auth-check") {
            val principal = call.principal<JWTPrincipal>() ?: throw IllegalStateException("no principal")
            val username = principal.payload.getClaim("uid")
            call.respondText("Hello $username")
        }
    }
}
