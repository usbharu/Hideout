package federation

import AssertionUtil
import KarateUtil
import com.intuit.karate.core.MockServer
import com.intuit.karate.junit5.Karate
import dev.usbharu.hideout.SpringApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(
    classes = [SpringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Transactional
class InboxCommonTest {
    @LocalServerPort
    private var port = ""

    @Karate.Test
    @TestFactory
    fun `inboxにHTTP Signature付きのリクエストがあったらリモートに取得しに行く`(): Karate {
        return KarateUtil.e2eTest(
            "InboxCommonTest",
            "inboxにHTTP Signature付きのリクエストがあったらリモートに取得しに行く",
            mapOf(
                "karate.port" to port,
                "karate.remotePort" to _remotePort
            ),
            javaClass
        )
    }

    @Karate.Test
    @TestFactory
    fun `user-inboxにHTTP Signature付きのリクエストがあったらリモートに取得しに行く`(): Karate {
        return KarateUtil.e2eTest(
            "InboxCommonTest",
            "user-inboxにHTTP Signature付きのリクエストがあったらリモートに取得しに行く",
            mapOf(
                "karate.port" to port,
                "karate.remotePort" to _remotePort
            ),
            javaClass
        )
    }

    @Karate.Test
    @TestFactory
    fun `inboxにHTTP Signatureがないリクエストがきたら401を返す`(): Karate {
        return KarateUtil.e2eTest(
            "InboxCommonTest",
            "inboxにHTTP Signatureがないリクエストがきたら401を返す",
            mapOf("karate.port" to port),
            javaClass
        )
    }

    @Karate.Test
    @TestFactory
    fun `user-inboxにHTTP Signatureがないリクエストがきたら401を返す`(): Karate {
        return KarateUtil.e2eTest(
            "InboxCommonTest",
            "user-inboxにHTTP Signatureがないリクエストがきたら401を返す",
            mapOf("karate.port" to port),
            javaClass
        )
    }

    @Karate.Test
    @TestFactory
    fun `inboxにConetnt-Type application *+json以外が来たら415を返す`(): Karate {
        return KarateUtil.e2eTest(
            "InboxCommonTest",
            "inboxにContent-Type application/json以外が来たら415を返す",
            mapOf("karate.port" to port),
            javaClass
        )
    }

    companion object {
        lateinit var server: MockServer

        lateinit var _remotePort: String

        @JvmStatic
        fun assertUserExist(username: String, domain: String) = runBlocking {
            var check = false

            repeat(10) {
                delay(1000)
                check = AssertionUtil.assertUserExist(username, domain) or check
                if (check) {
                    return@repeat
                }
            }

            assertTrue(check, "User @$username@$domain not exist.")
        }

        @JvmStatic
        fun getRemotePort(): String = _remotePort

        @BeforeAll
        @JvmStatic
        fun beforeAll(@Autowired flyway: Flyway) {
            server = MockServer.feature("classpath:federation/InboxxCommonMockServerTest.feature").http(0).build()
            _remotePort = server.port.toString()

            flyway.clean()
            flyway.migrate()
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            server.stop()
        }
    }
}
