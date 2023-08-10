@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.usbharu.hideout.service.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.config.ConfigData
import dev.usbharu.hideout.domain.model.hideout.entity.Jwt
import dev.usbharu.hideout.domain.model.hideout.entity.JwtRefreshToken
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.hideout.form.RefreshToken
import dev.usbharu.hideout.exception.InvalidRefreshTokenException
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.repository.IJwtRefreshTokenRepository
import dev.usbharu.hideout.service.core.IMetaService
import dev.usbharu.hideout.util.Base64Util
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class JwtServiceImplTest {
    @Test
    fun `createToken トークンを作成できる`() = runTest {
        Config.configData = ConfigData(url = "https://example.com", objectMapper = jacksonObjectMapper())
        val kid = UUID.randomUUID()
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val generateKeyPair = keyPairGenerator.generateKeyPair()

        val metaService = mock<IMetaService> {
            onBlocking { getJwtMeta() } doReturn Jwt(
                kid,
                Base64Util.encode(generateKeyPair.private.encoded),
                Base64Util.encode(generateKeyPair.public.encoded)
            )
        }
        val refreshTokenRepository = mock<IJwtRefreshTokenRepository> {
            onBlocking { generateId() } doReturn 1L
        }
        val jwtService = JwtServiceImpl(metaService, refreshTokenRepository, mock())
        val token = jwtService.createToken(
            User(
                id = 1L,
                name = "test",
                domain = "example.com",
                screenName = "testUser",
                description = "",
                password = "hashedPassword",
                inbox = "https://example.com/inbox",
                outbox = "https://example.com/outbox",
                url = "https://example.com",
                publicKey = "-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----",
                privateKey = "-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----",
                createdAt = Instant.now()
            )
        )
        assertNotEquals("", token.token)
        assertNotEquals("", token.refreshToken)
        val verify = JWT.require(
            Algorithm.RSA256(
                generateKeyPair.public as RSAPublicKey,
                generateKeyPair.private as RSAPrivateKey
            )
        )
            .withAudience("https://example.com/users/test")
            .withIssuer("https://example.com")
            .acceptLeeway(3L)
            .build()
            .verify(token.token)

        assertEquals(kid.toString(), verify.keyId)
    }

    @Test
    fun `refreshToken リフレッシュトークンからトークンを作成できる`() = runTest {
        Config.configData = ConfigData(url = "https://example.com", objectMapper = jacksonObjectMapper())
        val kid = UUID.randomUUID()
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val generateKeyPair = keyPairGenerator.generateKeyPair()

        val refreshTokenRepository = mock<IJwtRefreshTokenRepository> {
            onBlocking { findByToken("refreshToken") } doReturn JwtRefreshToken(
                id = 1L,
                userId = 1L,
                refreshToken = "refreshToken",
                createdAt = Instant.now().minus(60, ChronoUnit.MINUTES),
                expiresAt = Instant.now().plus(14, ChronoUnit.DAYS).minus(60, ChronoUnit.MINUTES)
            )
            onBlocking { generateId() } doReturn 2L
        }
        val userService = mock<UserQueryService> {
            onBlocking { findById(1L) } doReturn User(
                id = 1L,
                name = "test",
                domain = "example.com",
                screenName = "testUser",
                description = "",
                password = "hashedPassword",
                inbox = "https://example.com/inbox",
                outbox = "https://example.com/outbox",
                url = "https://example.com",
                publicKey = "-----BEGIN PUBLIC KEY-----...-----BEGIN PUBLIC KEY-----",
                privateKey = "-----BEGIN PRIVATE KEY-----...-----BEGIN PRIVATE KEY-----",
                createdAt = Instant.now()
            )
        }
        val metaService = mock<IMetaService> {
            onBlocking { getJwtMeta() } doReturn Jwt(
                kid,
                Base64Util.encode(generateKeyPair.private.encoded),
                Base64Util.encode(generateKeyPair.public.encoded)
            )
        }
        val jwtService = JwtServiceImpl(metaService, refreshTokenRepository, userService)
        val refreshToken = jwtService.refreshToken(RefreshToken("refreshToken"))
        assertNotEquals("", refreshToken.token)
        assertNotEquals("", refreshToken.refreshToken)

        val verify = JWT.require(
            Algorithm.RSA256(
                generateKeyPair.public as RSAPublicKey,
                generateKeyPair.private as RSAPrivateKey
            )
        )
            .withAudience("https://example.com/users/test")
            .withIssuer("https://example.com")
            .acceptLeeway(3L)
            .build()
            .verify(refreshToken.token)

        assertEquals(kid.toString(), verify.keyId)
    }

    @Test
    fun `refreshToken 無効なリフレッシュトークンは失敗する`() = runTest {
        val refreshTokenRepository = mock<IJwtRefreshTokenRepository> {
            onBlocking { findByToken("InvalidRefreshToken") } doReturn null
        }
        val jwtService = JwtServiceImpl(mock(), refreshTokenRepository, mock())
        assertThrows<InvalidRefreshTokenException> { jwtService.refreshToken(RefreshToken("InvalidRefreshToken")) }
    }

    @Test
    fun `refreshToken 未来に作成されたリフレッシュトークンは失敗する`() = runTest {
        val refreshTokenRepository = mock<IJwtRefreshTokenRepository> {
            onBlocking { findByToken("refreshToken") } doReturn JwtRefreshToken(
                id = 1L,
                userId = 1L,
                refreshToken = "refreshToken",
                createdAt = Instant.now().plus(10, ChronoUnit.MINUTES),
                expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES).plus(14, ChronoUnit.DAYS)
            )
        }
        val jwtService = JwtServiceImpl(mock(), refreshTokenRepository, mock())
        assertThrows<InvalidRefreshTokenException> { jwtService.refreshToken(RefreshToken("refreshToken")) }
    }

    @Test
    fun `refreshToken 期限切れのリフレッシュトークンでは失敗する`() = runTest {
        val refreshTokenRepository = mock<IJwtRefreshTokenRepository> {
            onBlocking { findByToken("refreshToken") } doReturn JwtRefreshToken(
                id = 1L,
                userId = 1L,
                refreshToken = "refreshToken",
                createdAt = Instant.now().minus(30, ChronoUnit.DAYS),
                expiresAt = Instant.now().minus(16, ChronoUnit.DAYS)
            )
        }
        val jwtService = JwtServiceImpl(mock(), refreshTokenRepository, mock())
        assertThrows<InvalidRefreshTokenException> { jwtService.refreshToken(RefreshToken("refreshToken")) }
    }
}
