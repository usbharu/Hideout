@file:Suppress("UnusedPrivateMember")

package dev.usbharu.hideout.plugins

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.JwtToken
import dev.usbharu.hideout.domain.model.hideout.entity.JwtRefreshToken
import dev.usbharu.hideout.domain.model.hideout.form.RefreshToken
import dev.usbharu.hideout.domain.model.hideout.form.UserLogin
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.property
import dev.usbharu.hideout.repository.IJwtRefreshTokenRepository
import dev.usbharu.hideout.repository.IMetaRepository
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.IUserAuthService
import dev.usbharu.hideout.service.IdGenerateService
import dev.usbharu.hideout.util.Base64Util
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
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

const val TOKEN_AUTH = "jwt-auth"

fun Application.configureSecurity(
    userAuthService: IUserAuthService,
    metaRepository: IMetaRepository,
    refreshTokenRepository: IJwtRefreshTokenRepository,
    userRepository: IUserRepository,
    idGenerateService: IdGenerateService
) {

    val privateKey = runBlocking {
        RsaUtil.decodeRsaPrivateKey(Base64Util.decode(requireNotNull(metaRepository.get()).jwt.privateKey))
    }
    val publicKey = runBlocking {
        val publicKey = requireNotNull(metaRepository.get()).jwt.publicKey
        RsaUtil.decodeRsaPublicKey(Base64Util.decode(publicKey))
    }
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

            val findByNameAndDomain = userRepository.findByNameAndDomain(user.username, Config.configData.domain)
                ?: throw UserNotFoundException("${user.username} was not found.")

            val token = JWT.create()
                .withAudience("${Config.configData.url}/users/${user.username}")
                .withIssuer(issuer)
                .withKeyId(metaRepository.get()?.jwt?.kid.toString())
                .withClaim("username", user.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.RSA256(publicKey, privateKey))
            val refreshToken = UUID.randomUUID().toString()
            refreshTokenRepository.save(
                JwtRefreshToken(
                    idGenerateService.generateId(), findByNameAndDomain.id, refreshToken, Instant.now(),
                    Instant.ofEpochMilli(Instant.now().toEpochMilli() + 1209600033)
                )
            )
            return@post call.respond(JwtToken(token, refreshToken))
        }

        post("/refresh-token") {
            val refreshToken = call.receive<RefreshToken>()
            val findByToken = refreshTokenRepository.findByToken(refreshToken.refreshToken)
                ?: return@post call.respondText("token not found",status = HttpStatusCode.Forbidden)

            if (findByToken.createdAt.isAfter(Instant.now())) {
                return@post call.respondText("created_at", status =  HttpStatusCode.Forbidden)
            }

            if (findByToken.expiresAt.isBefore(Instant.now())) {
                return@post call.respondText( "expires_at", status =  HttpStatusCode.Forbidden)
            }

            val user = userRepository.findById(findByToken.userId)
                ?: throw UserNotFoundException("${findByToken.userId} was not found.")
            val token = JWT.create()
                .withAudience("${Config.configData.url}/users/${user.name}")
                .withIssuer(issuer)
                .withKeyId(metaRepository.get()?.jwt?.kid.toString())
                .withClaim("username", user.name)
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.RSA256(publicKey, privateKey))
            val newRefreshToken = UUID.randomUUID().toString()
            refreshTokenRepository.save(
                JwtRefreshToken(
                    idGenerateService.generateId(), user.id, newRefreshToken, Instant.now(),
                    Instant.ofEpochMilli(Instant.now().toEpochMilli() + 1209600033)
                )
            )
            return@post call.respond(JwtToken(token, newRefreshToken))
        }

        get("/.well-known/jwks.json") {
            //language=JSON
            val meta = requireNotNull(metaRepository.get())
            call.respondText(
                contentType = ContentType.Application.Json,
                text = JsonWebKeyUtil.publicKeyToJwk(meta.jwt.publicKey, meta.jwt.kid.toString())
            )
        }
    }
}
