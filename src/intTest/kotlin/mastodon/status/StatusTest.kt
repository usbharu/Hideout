package mastodon.status

import dev.usbharu.hideout.SpringApplication
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/test-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class StatusTest {

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
    fun 投稿できる() {
        mockMvc
            .post("/api/v1/statuses") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"status":"hello"}"""
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun write_statusesスコープで投稿できる() {
        mockMvc
            .post("/api/v1/statuses") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"status":"hello"}"""
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:statuses"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun 権限がないと403() {
        mockMvc
            .post("/api/v1/statuses") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"status":"hello"}"""
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithAnonymousUser
    fun 匿名だと401() {
        mockMvc
            .post("/api/v1/statuses") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"status":"hello"}"""
                with(csrf())
            }
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithAnonymousUser
    fun 匿名の場合通常はcsrfが無いので403() {
        mockMvc
            .post("/api/v1/statuses") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"status":"hello"}"""
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun formでも投稿できる() {
        mockMvc
            .post("/api/v1/statuses") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("status", "hello")
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:statuses"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    @Sql("/sql/test-post.sql")
    fun in_reply_to_idを指定したら返信として処理される() {
        mockMvc
            .post("/api/v1/statuses") {
                contentType = MediaType.APPLICATION_JSON
                //language=JSON
                content = """{
  "status": "hello",
  "in_reply_to_id": "1"
}"""
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("\$.in_reply_to_id") { value("1") } }
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
