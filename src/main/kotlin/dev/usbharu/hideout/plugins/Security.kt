@file:Suppress("UnusedPrivateMember")

package dev.usbharu.hideout.plugins

import com.auth0.jwk.JwkProviderBuilder
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.form.RefreshToken
import dev.usbharu.hideout.domain.model.hideout.form.UserLogin
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.property
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
import java.util.concurrent.TimeUnit

const val TOKEN_AUTH = "jwt-auth"

@Suppress("MagicNumber")
fun Application.configureSecurity(
    userAuthService: IUserAuthService,
    metaService: IMetaService,
    userRepository: IUserRepository,
    jwtService: IJwtService
) {
    val issuer = property("hideout.url")
    val jwkProvider = JwkProviderBuilder(issuer)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()
    install(Authentication) {
        jwt(TOKEN_AUTH) {
            verifier(jwkProvider, issuer) {
                acceptLeeway(3)
            }
            validate { jwtCredential ->
                if (jwtCredential.payload.getClaim("username").asString().isNotEmpty()) {
                    JWTPrincipal(jwtCredential.payload)
                } else {
                    null
                }
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
    }
}
