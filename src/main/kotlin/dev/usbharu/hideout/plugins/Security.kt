package dev.usbharu.hideout.plugins

import com.auth0.jwk.JwkProvider
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.form.RefreshToken
import dev.usbharu.hideout.domain.model.hideout.form.UserLogin
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.IJwtService
import dev.usbharu.hideout.service.IMetaService
import dev.usbharu.hideout.service.IUserAuthService
import dev.usbharu.hideout.util.JsonWebKeyUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val TOKEN_AUTH = "jwt-auth"

@Suppress("MagicNumber")
fun Application.configureSecurity(
    userAuthService: IUserAuthService,
    metaService: IMetaService,
    userRepository: IUserRepository,
    jwtService: IJwtService,
    jwkProvider: JwkProvider
) {
    val issuer = Config.configData.url
    install(Authentication) {
        jwt(TOKEN_AUTH) {
            verifier(jwkProvider, issuer) {
                acceptLeeway(3)
            }
            validate { jwtCredential ->
                val uid = jwtCredential.payload.getClaim("uid")
                if (uid.isMissing) {
                    return@validate null
                }
                if (uid.asLong() == null) {
                    return@validate null
                }
                return@validate JWTPrincipal(jwtCredential.payload)
            }
        }
    }

    routing {
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

        get("/.well-known/jwks.json") {
            //language=JSON
            val jwt = metaService.getJwtMeta()
            call.respondText(
                contentType = ContentType.Application.Json,
                text = JsonWebKeyUtil.publicKeyToJwk(jwt.publicKey, jwt.kid.toString())
            )
        }
        authenticate(TOKEN_AUTH) {
            get("/auth-check") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("uid")
                call.respondText("Hello $username")
            }
        }
    }
}
