package mastodon.account

import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.core.infrastructure.exposedquery.UserQueryServiceImpl
import kotlinx.coroutines.test.runTest
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/test-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class AccountApiTest {

    @Autowired
    private lateinit var userQueryServiceImpl: UserQueryServiceImpl

    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    fun `apiV1AccountsVerifyCredentialsGetにreadでアクセスできる`() {
        mockMvc
            .get("/api/v1/accounts/verify_credentials") {
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read")))
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV1AccountsVerifyCredentialsGetにread_accountsでアクセスできる`() {
        mockMvc
            .get("/api/v1/accounts/verify_credentials") {
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:accounts")))
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }
    }

    @Test
    @WithAnonymousUser
    fun apiV1AccountsVerifyCredentialsGetに匿名でアクセスすると401() {
        mockMvc
            .get("/api/v1/accounts/verify_credentials")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithAnonymousUser
    fun apiV1AccountsPostに匿名でPOSTしたらアカウントを作成できる() = runTest {
        mockMvc
            .post("/api/v1/accounts") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("username", "api-test-user-1")
                param("password", "very-secure-password")
                param("email", "test@example.com")
                param("agreement", "true")
                param("locale", "")
                with(SecurityMockMvcRequestPostProcessors.csrf())
            }
            .asyncDispatch()
            .andExpect { status { isFound() } }

        userQueryServiceImpl.findByNameAndDomain("api-test-user-1", "localhost")
    }

    @Test
    @WithAnonymousUser
    fun apiV1AccountsPostで必須パラメーター以外を省略しても作成できる() = runTest {
        mockMvc
            .post("/api/v1/accounts") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("username", "api-test-user-2")
                param("password", "very-secure-password")
                with(SecurityMockMvcRequestPostProcessors.csrf())
            }
            .asyncDispatch()
            .andExpect { status { isFound() } }

        userQueryServiceImpl.findByNameAndDomain("api-test-user-2", "localhost")
    }

    @Test
    @WithAnonymousUser
    fun apiV1AccountsPostでusernameパラメーターを省略したら400() = runTest {
        mockMvc
            .post("/api/v1/accounts") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("username", "api-test-user-3")
                with(SecurityMockMvcRequestPostProcessors.csrf())
            }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    @WithAnonymousUser
    fun apiV1AccountsPostでpasswordパラメーターを省略したら400() = runTest {
        mockMvc
            .post("/api/v1/accounts") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("username", "api-test-user-3")
                with(SecurityMockMvcRequestPostProcessors.csrf())
            }
            .andExpect { status { isBadRequest() } }
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
