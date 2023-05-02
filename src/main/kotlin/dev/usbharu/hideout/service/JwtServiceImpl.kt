package dev.usbharu.hideout.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.JwtToken
import dev.usbharu.hideout.domain.model.hideout.entity.JwtRefreshToken
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.hideout.form.RefreshToken
import dev.usbharu.hideout.exception.InvalidRefreshTokenException
import dev.usbharu.hideout.repository.IJwtRefreshTokenRepository
import dev.usbharu.hideout.service.impl.IUserService
import dev.usbharu.hideout.util.RsaUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class JwtServiceImpl(
    private val metaService: IMetaService,
    private val refreshTokenRepository: IJwtRefreshTokenRepository,
    private val userService: IUserService
) : IJwtService {

    private val privateKey by lazy {
        CoroutineScope(Dispatchers.IO).async {
            RsaUtil.decodeRsaPrivateKey(metaService.getJwtMeta().privateKey)
        }
    }

    private val publicKey by lazy {
        CoroutineScope(Dispatchers.IO).async {
            RsaUtil.decodeRsaPublicKey(metaService.getJwtMeta().publicKey)
        }
    }

    private val keyId by lazy {
        CoroutineScope(Dispatchers.IO).async {
            metaService.getJwtMeta().kid
        }
    }

    override suspend fun createToken(user: User): JwtToken {
        val now = Instant.now()
        val token = JWT.create()
            .withAudience("${Config.configData.url}/users/${user.id}")
            .withIssuer(Config.configData.url)
            .withKeyId(keyId.await().toString())
            .withClaim("username", user.name)
            .withExpiresAt(now.plus(30, ChronoUnit.MINUTES))
            .sign(Algorithm.RSA256(publicKey.await(), privateKey.await()))

        val jwtRefreshToken = JwtRefreshToken(
            id = refreshTokenRepository.generateId(),
            userId = user.id,
            refreshToken = UUID.randomUUID().toString(),
            createdAt = now,
            expiresAt = now.plus(14, ChronoUnit.DAYS)
        )
        refreshTokenRepository.save(jwtRefreshToken)
        return JwtToken(token, jwtRefreshToken.refreshToken)
    }

    override suspend fun refreshToken(refreshToken: RefreshToken): JwtToken {
        val token = refreshTokenRepository.findByToken(refreshToken.refreshToken)
            ?: throw InvalidRefreshTokenException("Invalid Refresh Token")

        val user = userService.findById(token.userId)

        val now = Instant.now()
        if (token.createdAt.isAfter(now)) {
            throw InvalidRefreshTokenException("Invalid Refresh Token")
        }

        if (token.expiresAt.isBefore(now)) {
            throw InvalidRefreshTokenException("Refresh Token Expired")
        }

        return createToken(user)
    }

    override suspend fun revokeToken(refreshToken: RefreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken.refreshToken)
    }

    override suspend fun revokeToken(user: User) {
        refreshTokenRepository.deleteByUserId(user.id)
    }

    override suspend fun revokeAll() {
        refreshTokenRepository.deleteAll()
    }
}
