package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.user.toPem
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import utils.TestTransaction
import java.security.KeyPairGenerator
import java.time.Instant

class KtorKeyMapTest {

    @Test
    fun getPrivateKey() {
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
                    createdAt = Instant.now()
                )
            }
        }
        val ktorKeyMap = KtorKeyMap(userQueryService, TestTransaction)

        ktorKeyMap.getPrivateKey("test")
    }
}
