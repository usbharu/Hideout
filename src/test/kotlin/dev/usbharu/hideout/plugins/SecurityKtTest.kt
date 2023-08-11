package dev.usbharu.hideout.plugins

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.config.ConfigData
import dev.usbharu.hideout.domain.model.hideout.dto.JwtToken
import dev.usbharu.hideout.domain.model.hideout.entity.Jwt
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.hideout.form.RefreshToken
import dev.usbharu.hideout.domain.model.hideout.form.UserLogin
import dev.usbharu.hideout.exception.InvalidRefreshTokenException
import dev.usbharu.hideout.exception.InvalidUsernameOrPasswordException
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.routing.api.internal.v1.auth
import dev.usbharu.hideout.service.api.UserAuthApiService
import dev.usbharu.hideout.service.auth.JwtService
import dev.usbharu.hideout.service.core.MetaService
import dev.usbharu.hideout.service.user.UserAuthService
import dev.usbharu.hideout.util.Base64Util
import dev.usbharu.hideout.util.JsonWebKeyUtil
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.assertEquals

class SecurityKtTest {
    @Test
    fun `login ログイン出来るか`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        Config.configData = ConfigData(url = "http://example.com", objectMapper = jacksonObjectMapper())
        val jwtToken = JwtToken("Token", "RefreshToken")
        val userAuthService = mock<UserAuthApiService> {
            onBlocking { login(eq("testUser"), eq("password")) } doReturn jwtToken
        }
        val metaService = mock<MetaService>()
        val userQueryService = mock<UserQueryService> {
            onBlocking { findByNameAndDomain(eq("testUser"), eq("example.com")) } doReturn User(
                id = 1L,
                name = "testUser",
                domain = "example.com",
                screenName = "test",
                description = "",
                password = "hashedPassword",
                inbox = "https://example.com/inbox",
                outbox = "https://example.com/outbox",
                url = "https://example.com/profile",
                publicKey = "",
                privateKey = "",
                createdAt = Instant.now()
            )
        }
        val jwkProvider = mock<JwkProvider>()
        application {
            configureSerialization()
            configureSecurity(jwkProvider, metaService)
            routing {
                auth(userAuthService)
            }
        }

        client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(Config.configData.objectMapper.writeValueAsString(UserLogin("testUser", "password")))
        }.apply {
            assertEquals(HttpStatusCode.OK, call.response.status)
            assertEquals(jwtToken, Config.configData.objectMapper.readValue(call.response.bodyAsText()))
        }
    }

    @Test
    fun `login 存在しないユーザーのログインに失敗する`() {
        testApplication {
            environment {
                config = ApplicationConfig("empty.conf")
            }
            Config.configData = ConfigData(url = "http://example.com", objectMapper = jacksonObjectMapper())
            mock<UserAuthService> {
                onBlocking { verifyAccount(anyString(), anyString()) }.doReturn(false)
            }
            val metaService = mock<MetaService>()
            mock<UserQueryService>()
            mock<JwtService>()
            val jwkProvider = mock<JwkProvider>()
            val userAuthApiService = mock<UserAuthApiService> {
                onBlocking { login(anyString(), anyString()) } doThrow InvalidUsernameOrPasswordException()
            }
            application {
                configureStatusPages()
                configureSerialization()
                configureSecurity(jwkProvider, metaService)
                routing {
                    auth(userAuthApiService)
                }
            }
            client.post("/login") {
                contentType(ContentType.Application.Json)
                setBody(Config.configData.objectMapper.writeValueAsString(UserLogin("InvalidTtestUser", "password")))
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, call.response.status)
            }
        }
    }

    @Test
    fun `login 不正なパスワードのログインに失敗する`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        Config.configData = ConfigData(url = "http://example.com", objectMapper = jacksonObjectMapper())
        val metaService = mock<MetaService>()
        val jwkProvider = mock<JwkProvider>()
        val userAuthApiService = mock<UserAuthApiService> {
            onBlocking { login(anyString(), eq("InvalidPassword")) } doThrow InvalidUsernameOrPasswordException()
        }
        application {
            configureStatusPages()
            configureSerialization()
            configureSecurity(jwkProvider, metaService)
            routing {
                auth(userAuthApiService)
            }
        }
        client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(Config.configData.objectMapper.writeValueAsString(UserLogin("TestUser", "InvalidPassword")))
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, call.response.status)
        }
    }

    @Test
    fun `auth-check Authorizedヘッダーが無いと401が帰ってくる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        Config.configData = ConfigData(url = "http://example.com", objectMapper = jacksonObjectMapper())
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                auth(mock())
            }
        }
        client.get("/auth-check").apply {
            assertEquals(HttpStatusCode.Unauthorized, call.response.status)
        }
    }

    @Test
    fun `auth-check Authorizedヘッダーの形式が間違っていると401が帰ってくる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        Config.configData = ConfigData(url = "http://example.com", objectMapper = jacksonObjectMapper())
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                auth(mock())
            }
        }
        client.get("/auth-check") {
            header("Authorization", "Digest dsfjjhogalkjdfmlhaog")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, call.response.status)
        }
    }

    @Test
    fun `auth-check Authorizedヘッダーが空だと401が帰ってくる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        Config.configData = ConfigData(url = "http://example.com", objectMapper = jacksonObjectMapper())
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                auth(mock())
            }
        }
        client.get("/auth-check") {
            header("Authorization", "")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, call.response.status)
        }
    }

    @Test
    fun `auth-check AuthorizedヘッダーがBeararで空だと401が帰ってくる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        Config.configData = ConfigData(url = "http://example.com", objectMapper = jacksonObjectMapper())

        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                auth(mock())
            }
        }
        client.get("/auth-check") {
            header("Authorization", "Bearer ")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, call.response.status)
        }
    }

    @Test
    fun `auth-check 正当なJWTだとアクセスできる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        val rsaPublicKey = keyPair.public as RSAPublicKey

        Config.configData = ConfigData(url = "https://localhost", objectMapper = jacksonObjectMapper())

        val now = Instant.now()
        val kid = UUID.randomUUID()
        val token = JWT.create()
            .withAudience("${Config.configData.url}/users/test")
            .withIssuer(Config.configData.url)
            .withKeyId(kid.toString())
            .withClaim("uid", 123456L)
            .withExpiresAt(now.plus(30, ChronoUnit.MINUTES))
            .sign(Algorithm.RSA256(rsaPublicKey, keyPair.private as RSAPrivateKey))
        val metaService = mock<MetaService> {
            onBlocking { getJwtMeta() }.doReturn(
                Jwt(
                    kid,
                    Base64Util.encode(keyPair.private.encoded),
                    Base64Util.encode(rsaPublicKey.encoded)
                )
            )
        }

        val readValue = Config.configData.objectMapper.readerFor(Map::class.java)
            .readValue<MutableMap<String, Any>?>(
                JsonWebKeyUtil.publicKeyToJwk(
                    rsaPublicKey,
                    kid.toString()
                )
            )
        val jwkProvider = mock<JwkProvider> {
            onBlocking { get(anyString()) }.doReturn(
                Jwk.fromValues(
                    (readValue["keys"] as List<Map<String, Any>>)[0]
                )
            )
        }
        application {
            configureSerialization()
            configureSecurity(jwkProvider, metaService)
            routing {
                auth(mock())
            }
        }

        client.get("/auth-check") {
            header("Authorization", "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, call.response.status)
            assertEquals("Hello 123456", call.response.bodyAsText())
        }
    }

    @Test
    fun `auth-check 期限切れのトークンではアクセスできない`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        val rsaPublicKey = keyPair.public as RSAPublicKey

        Config.configData = ConfigData(url = "https://localhost", objectMapper = jacksonObjectMapper())

        val now = Instant.now()
        val kid = UUID.randomUUID()
        val token = JWT.create()
            .withAudience("${Config.configData.url}/users/test")
            .withIssuer(Config.configData.url)
            .withKeyId(kid.toString())
            .withClaim("uid", 123345L)
            .withExpiresAt(now.minus(30, ChronoUnit.MINUTES))
            .sign(Algorithm.RSA256(rsaPublicKey, keyPair.private as RSAPrivateKey))
        val metaService = mock<MetaService> {
            onBlocking { getJwtMeta() }.doReturn(
                Jwt(
                    kid,
                    Base64Util.encode(keyPair.private.encoded),
                    Base64Util.encode(rsaPublicKey.encoded)
                )
            )
        }

        val readValue = Config.configData.objectMapper.readerFor(Map::class.java)
            .readValue<MutableMap<String, Any>?>(
                JsonWebKeyUtil.publicKeyToJwk(
                    rsaPublicKey,
                    kid.toString()
                )
            )
        val jwkProvider = mock<JwkProvider> {
            onBlocking { get(anyString()) }.doReturn(
                Jwk.fromValues(
                    (readValue["keys"] as List<Map<String, Any>>)[0]
                )
            )
        }
        application {
            configureSerialization()
            configureSecurity(jwkProvider, metaService)
            routing {
                auth(mock())
            }
        }
        client.get("/auth-check") {
            header("Authorization", "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, call.response.status)
        }
    }

    @Test
    fun `auth-check issuerが間違っているとアクセスできない`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        val rsaPublicKey = keyPair.public as RSAPublicKey

        Config.configData = ConfigData(url = "https://localhost", objectMapper = jacksonObjectMapper())

        val now = Instant.now()
        val kid = UUID.randomUUID()
        val token = JWT.create()
            .withAudience("${Config.configData.url}/users/test")
            .withIssuer("https://example.com")
            .withKeyId(kid.toString())
            .withClaim("uid", 12345L)
            .withExpiresAt(now.plus(30, ChronoUnit.MINUTES))
            .sign(Algorithm.RSA256(rsaPublicKey, keyPair.private as RSAPrivateKey))
        val metaService = mock<MetaService> {
            onBlocking { getJwtMeta() }.doReturn(
                Jwt(
                    kid,
                    Base64Util.encode(keyPair.private.encoded),
                    Base64Util.encode(rsaPublicKey.encoded)
                )
            )
        }

        val readValue = Config.configData.objectMapper.readerFor(Map::class.java)
            .readValue<MutableMap<String, Any>?>(
                JsonWebKeyUtil.publicKeyToJwk(
                    rsaPublicKey,
                    kid.toString()
                )
            )
        val jwkProvider = mock<JwkProvider> {
            onBlocking { get(anyString()) }.doReturn(
                Jwk.fromValues(
                    (readValue["keys"] as List<Map<String, Any>>)[0]
                )
            )
        }
        application {
            configureSerialization()
            configureSecurity(jwkProvider, metaService)
            routing {
                auth(mock())
            }
        }
        client.get("/auth-check") {
            header("Authorization", "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, call.response.status)
        }
    }

    @Test
    fun `auth-check usernameが空だと失敗する`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        val rsaPublicKey = keyPair.public as RSAPublicKey

        Config.configData = ConfigData(url = "https://localhost", objectMapper = jacksonObjectMapper())

        val now = Instant.now()
        val kid = UUID.randomUUID()
        val token = JWT.create()
            .withAudience("${Config.configData.url}/users/test")
            .withIssuer(Config.configData.url)
            .withKeyId(kid.toString())
            .withClaim("uid", null as Long?)
            .withExpiresAt(now.plus(30, ChronoUnit.MINUTES))
            .sign(Algorithm.RSA256(rsaPublicKey, keyPair.private as RSAPrivateKey))
        val metaService = mock<MetaService> {
            onBlocking { getJwtMeta() }.doReturn(
                Jwt(
                    kid,
                    Base64Util.encode(keyPair.private.encoded),
                    Base64Util.encode(rsaPublicKey.encoded)
                )
            )
        }

        val readValue = Config.configData.objectMapper.readerFor(Map::class.java)
            .readValue<MutableMap<String, Any>?>(
                JsonWebKeyUtil.publicKeyToJwk(
                    rsaPublicKey,
                    kid.toString()
                )
            )
        val jwkProvider = mock<JwkProvider> {
            onBlocking { get(anyString()) }.doReturn(
                Jwk.fromValues(
                    (readValue["keys"] as List<Map<String, Any>>)[0]
                )
            )
        }
        application {
            configureSerialization()
            configureSecurity(jwkProvider, metaService)
            routing {
                auth(mock())
            }
        }
        client.get("/auth-check") {
            header("Authorization", "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, call.response.status)
        }
    }

    @Test
    fun `auth-check usernameが存在しないと失敗する`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        val rsaPublicKey = keyPair.public as RSAPublicKey

        Config.configData = ConfigData(url = "https://localhost", objectMapper = jacksonObjectMapper())

        val now = Instant.now()
        val kid = UUID.randomUUID()
        val token = JWT.create()
            .withAudience("${Config.configData.url}/users/test")
            .withIssuer(Config.configData.url)
            .withKeyId(kid.toString())
            .withExpiresAt(now.plus(30, ChronoUnit.MINUTES))
            .sign(Algorithm.RSA256(rsaPublicKey, keyPair.private as RSAPrivateKey))
        val metaService = mock<MetaService> {
            onBlocking { getJwtMeta() }.doReturn(
                Jwt(
                    kid,
                    Base64Util.encode(keyPair.private.encoded),
                    Base64Util.encode(rsaPublicKey.encoded)
                )
            )
        }

        val readValue = Config.configData.objectMapper.readerFor(Map::class.java)
            .readValue<MutableMap<String, Any>?>(
                JsonWebKeyUtil.publicKeyToJwk(
                    rsaPublicKey,
                    kid.toString()
                )
            )
        val jwkProvider = mock<JwkProvider> {
            onBlocking { get(anyString()) }.doReturn(
                Jwk.fromValues(
                    (readValue["keys"] as List<Map<String, Any>>)[0]
                )
            )
        }
        application {
            configureSerialization()
            configureSecurity(jwkProvider, metaService)
            routing {
                auth(mock())
            }
        }
        client.get("/auth-check") {
            header("Authorization", "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, call.response.status)
        }
    }

    @Test
    fun `refresh-token リフレッシュトークンが正当だとトークンを発行する`() = testApplication {
        Config.configData = ConfigData(url = "https://localhost", objectMapper = jacksonObjectMapper())
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val jwtService = mock<UserAuthApiService> {
            onBlocking { refreshToken(any()) }.doReturn(JwtToken("token", "refreshToken2"))
        }
        application {
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                auth(jwtService)
            }
        }
        client.post("/refresh-token") {
            header("Content-Type", "application/json")
            setBody(Config.configData.objectMapper.writeValueAsString(RefreshToken("refreshToken")))
        }.apply {
            assertEquals(HttpStatusCode.OK, call.response.status)
        }
    }

    @Test
    fun `refresh-token リフレッシュトークンが不正だと失敗する`() = testApplication {
        Config.configData = ConfigData(url = "https://localhost", objectMapper = jacksonObjectMapper())
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val jwtService = mock<UserAuthApiService> {
            onBlocking { refreshToken(any()) } doThrow InvalidRefreshTokenException("Invalid Refresh Token")
        }
        application {
            configureStatusPages()
            configureSerialization()
            configureSecurity(mock(), mock())
            routing {
                auth(jwtService)
            }
        }
        client.post("/refresh-token") {
            header("Content-Type", "application/json")
            setBody(Config.configData.objectMapper.writeValueAsString(RefreshToken("InvalidRefreshToken")))
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, call.response.status)
        }
    }
}
