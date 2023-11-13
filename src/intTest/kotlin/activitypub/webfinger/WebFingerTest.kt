package activitypub.webfinger

import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.application.external.Transaction
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import util.TestTransaction
import java.net.URL

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
class WebFingerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @Sql("/sql/test-user.sql")
    fun `webfinger 存在するユーザーを取得`() {
        mockMvc
            .get("/.well-known/webfinger?resource=acct:test-user@example.com")
            .andExpect { status { isOk() } }
            .andExpect { header { string("Content-Type", "application/json") } }
            .andExpect {
                jsonPath("\$.subject") {
                    value("acct:test-user@example.com")
                }
            }
            .andExpect {
                jsonPath("\$.links[0].rel") {
                    value("self")
                }
            }
            .andExpect {
                jsonPath("\$.links[0].href") { value("https://example.com/users/test-user") }
            }
            .andExpect {
                jsonPath("\$.links[0].type") {
                    value("application/activity+json")
                }
            }
    }

    @Test
    fun `webfinger 存在しないユーザーに404`() {
        mockMvc
            .get("/.well-known/webfinger?resource=acct:test-user@example.com")
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `webfinger 不正なリクエストは400`() {
        mockMvc
            .get("/.well-known/webfinger?res=acct:test")
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `webfinger acctのパースが出来なくても400`() {
        mockMvc
            .get("/.well-known/webfinger?resource=acct:@a@b@c@d")
            .andExpect { status { isBadRequest() } }
    }

    @TestConfiguration
    class Configuration {
        @Bean
        fun url(): URL {
            return URL("https://example.com")
        }

        @Bean
        fun testTransaction(): Transaction = TestTransaction
    }
}
