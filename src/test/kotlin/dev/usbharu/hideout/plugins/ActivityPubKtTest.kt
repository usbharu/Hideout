package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.domain.model.ap.JsonLd
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.user.toPem
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.logging.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import utils.TestTransaction
import java.security.KeyPairGenerator
import java.time.Instant

class ActivityPubKtTest {
    @Test
    fun HttpSignTest() {
        val userQueryService = mock<UserQueryService> {
            onBlocking { findByNameAndDomain(any(), any()) } doAnswer {
                val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
                keyPairGenerator.initialize(1024)
                val generateKeyPair = keyPairGenerator.generateKeyPair()
                User(
                    1,
                    "test",
                    "localhost",
                    "test",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    generateKeyPair.private.toPem(),
                    Instant.now()
                )
            }
        }
        runBlocking {
            val ktorKeyMap = KtorKeyMap(userQueryService, TestTransaction)

            val httpClient = HttpClient(
                MockEngine { httpRequestData ->
                    respondOk()
                }
            ) {
                install(httpSignaturePlugin) {
                    keyMap = ktorKeyMap
                }
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.ALL
                }
            }

            httpClient.postAp("https://localhost", "test", JsonLd(emptyList()))
        }
    }
}
