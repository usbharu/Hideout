@file:Suppress("UnusedPrivateMember")

package dev.usbharu.hideout.plugins

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.usbharu.hideout.domain.model.hideout.form.UserLogin
import dev.usbharu.hideout.property
import dev.usbharu.hideout.service.IUserAuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.concurrent.TimeUnit

const val TOKEN_AUTH = "jwt-auth"

fun Application.configureSecurity(userAuthService: IUserAuthService) {

    val privateKeyString = property("jwt.privateKey")
    val issuer = property("jwt.issuer")
//    val audience = property("jwt.audience")
    val myRealm = property("jwt.realm")
    val jwkProvider = JwkProviderBuilder(issuer)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()
    install(Authentication) {
        jwt(TOKEN_AUTH) {
            realm = myRealm
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
            val user = call.receive<UserLogin>()
            val check = userAuthService.verifyAccount(user.username, user.password)
            if (check.not()) {
                return@post call.respond(HttpStatusCode.Unauthorized)
            }

            val publicKey = jwkProvider.get("6f8856ed-9189-488f-9011-0ff4b6c08edc").publicKey
            val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString))
            val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
            val token = JWT.create()
//                .withAudience(audience)
//                .withIssuer(issuer)
                .withClaim("username", user.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey as RSAPrivateKey))
            return@post call.respond(hashSetOf("token" to token))
        }

        get("/.well-known/jwks.json"){
            //language=JSON
            call.respondText(contentType = ContentType.Application.Json,text = """{
  "keys": [
    {
      "kty": "RSA",
      "e": "AQAB",
      "kid": "6f8856ed-9189-488f-9011-0ff4b6c08edc",
      "n":"tfJaLrzXILUg1U3N1KV8yJr92GHn5OtYZR7qWk1Mc4cy4JGjklYup7weMjBD9f3bBVoIsiUVX6xNcYIr0Ie0AQ"
    }
  ]
}""")
        }
    }
}
