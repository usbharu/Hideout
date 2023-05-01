@file:Suppress("UnusedPrivateMember")

package dev.usbharu.hideout.plugins

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.form.UserLogin
import dev.usbharu.hideout.property
import dev.usbharu.hideout.repository.IMetaRepository
import dev.usbharu.hideout.service.IUserAuthService
import dev.usbharu.hideout.util.JsonWebKeyUtil
import dev.usbharu.hideout.util.RsaUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.concurrent.TimeUnit

const val TOKEN_AUTH = "jwt-auth"

fun Application.configureSecurity(userAuthService: IUserAuthService, metaRepository: IMetaRepository) {

    val privateKeyString = runBlocking {
        requireNotNull(metaRepository.get()).jwt.privateKey
    }
    val publicKey = runBlocking {
        val publicKey = requireNotNull(metaRepository.get()).jwt.publicKey
        println(publicKey)
        RsaUtil.decodeRsaPublicKey(Base64.getDecoder().decode(publicKey))
    }
    println(privateKeyString)
    val issuer = property("hideout.url")
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
            challenge { defaultScheme, realm ->
                call.respondRedirect("/login")
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
            val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString))
            val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
            val token = JWT.create()
                .withAudience("${Config.configData.url}/users/${user.username}")
                .withIssuer(issuer)
                .withKeyId(metaRepository.get()?.jwt?.kid.toString())
                .withClaim("username", user.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.RSA256(publicKey, privateKey as RSAPrivateKey))
            return@post call.respond(token)
        }

        get("/.well-known/jwks.json") {
            //language=JSON
            val meta = requireNotNull(metaRepository.get())
            call.respondText(
                contentType = ContentType.Application.Json,
                text = JsonWebKeyUtil.publicKeyToJwk(meta.jwt.publicKey,meta.jwt.kid.toString())
            )
        }
    }
}
