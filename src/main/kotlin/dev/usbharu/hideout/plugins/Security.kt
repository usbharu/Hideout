package dev.usbharu.hideout.plugins

import com.auth0.jwk.JwkProvider
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.service.core.IMetaService
import dev.usbharu.hideout.util.JsonWebKeyUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val TOKEN_AUTH = "jwt-auth"

@Suppress("MagicNumber")
fun Application.configureSecurity(
    jwkProvider: JwkProvider,
    metaService: IMetaService
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
