package mastodon.apps

import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.RegisteredClient
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.select
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
class AppTest {

    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc


    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @Test
    @WithAnonymousUser
    fun apiV1AppsPostにformで匿名でappを作成できる() {
        mockMvc
            .post("/api/v1/apps") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("client_name", "test-client")
                param("redirect_uris", "https://example.com")
                param("scopes", "write read")
                param("website", "https://example.com")
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }


        val app = RegisteredClient
            .select { RegisteredClient.clientName eq "test-client" }
            .single()

        assertThat(app[RegisteredClient.clientName]).isEqualTo("test-client")
        assertThat(app[RegisteredClient.redirectUris]).isEqualTo("https://example.com")
        assertThat(app[RegisteredClient.scopes]).isEqualTo("read,write")
    }

    @Test
    @WithAnonymousUser
    fun apiV1AppsPostにjsonで匿名でappを作成できる() {
        mockMvc
            .post("/api/v1/apps") {
                contentType = MediaType.APPLICATION_JSON
                content = """{
  "client_name": "test-client-2",
  "redirect_uris": "https://example.com",
  "scopes": "write read",
  "website": "https;//example.com"
}"""
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }

        val app = RegisteredClient
            .select { RegisteredClient.clientName eq "test-client-2" }
            .single()

        assertThat(app[RegisteredClient.clientName]).isEqualTo("test-client-2")
        assertThat(app[RegisteredClient.redirectUris]).isEqualTo("https://example.com")
        assertThat(app[RegisteredClient.scopes]).isEqualTo("read,write")
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
