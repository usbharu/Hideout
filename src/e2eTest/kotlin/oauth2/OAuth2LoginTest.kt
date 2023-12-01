package oauth2

import KarateUtil
import com.intuit.karate.junit5.Karate
import dev.usbharu.hideout.SpringApplication
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql

@SpringBootTest(
    classes = [SpringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@Sql("/oauth2/user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class OAuth2LoginTest {

    @LocalServerPort
    private var port = ""

    @Karate.Test
    @TestFactory
    fun test(): Karate =
        Karate.run("test").scenarioName("invalid").relativeTo(javaClass).systemProperty("karate.port", port)
            .karateEnv("dev")

    @Karate.Test
    @TestFactory
    fun `スコープwrite readを持ったトークンの作成`(): Karate {
        return KarateUtil.springBootKarateTest(
            "Oauth2LoginTest",
            "スコープwrite readを持ったトークンの作成",
            javaClass,
            port
        )
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun dropDatabase(@Autowired flyway: Flyway) {
            flyway.clean()
            flyway.migrate()
        }
    }
}
