package dev.usbharu.hideout.service.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.JwtToken
import dev.usbharu.hideout.domain.model.hideout.entity.JwtRefreshToken
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.hideout.form.RefreshToken
import dev.usbharu.hideout.exception.InvalidRefreshTokenException
import dev.usbharu.hideout.query.JwtRefreshTokenQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.repository.JwtRefreshTokenRepository
import dev.usbharu.hideout.service.core.MetaService
import dev.usbharu.hideout.util.RsaUtil
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
interface JwtService {
    suspend fun createToken(user: User): JwtToken
    suspend fun refreshToken(refreshToken: RefreshToken): JwtToken

    suspend fun revokeToken(refreshToken: RefreshToken)
    suspend fun revokeToken(user: User)
    suspend fun revokeAll()
}

@Suppress("InjectDispatcher")
@Service
class JwtServiceImpl(
    private val metaService: MetaService,
    private val refreshTokenRepository: JwtRefreshTokenRepository,
    private val userQueryService: UserQueryService,
    private val refreshTokenQueryService: JwtRefreshTokenQueryService
) : JwtService {

    private val privateKey = runBlocking {
        RsaUtil.decodeRsaPrivateKey(metaService.getJwtMeta().privateKey)
    }

    private val publicKey = runBlocking {
        RsaUtil.decodeRsaPublicKey(metaService.getJwtMeta().publicKey)
    }

    private val keyId = runBlocking { metaService.getJwtMeta().kid }

    @Suppress("MagicNumber")
    override suspend fun createToken(user: User): JwtToken {
        val now = Instant.now()
        val token = JWT.create()
            .withAudience("${Config.configData.url}/users/${user.name}")
            .withIssuer(Config.configData.url)
            .withKeyId(keyId.toString())
            .withClaim("uid", user.id)
            .withExpiresAt(now.plus(30, ChronoUnit.MINUTES))
            .sign(Algorithm.RSA256(publicKey, privateKey))

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
        val token = try {
            refreshTokenQueryService.findByToken(refreshToken.refreshToken)
        } catch (_: NoSuchElementException) {
            throw InvalidRefreshTokenException("Invalid Refresh Token")
        }

        val user = userQueryService.findById(token.userId)

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
        refreshTokenQueryService.deleteByToken(refreshToken.refreshToken)
    }

    override suspend fun revokeToken(user: User) {
        refreshTokenQueryService.deleteByUserId(user.id)
    }

    override suspend fun revokeAll() {
        refreshTokenQueryService.deleteAll()
    }
}
