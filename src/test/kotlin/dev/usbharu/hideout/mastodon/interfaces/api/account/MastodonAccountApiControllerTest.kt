package dev.usbharu.hideout.mastodon.interfaces.api.account

import dev.usbharu.hideout.application.config.ActivityPubConfig
import dev.usbharu.hideout.core.infrastructure.springframework.security.OAuth2JwtLoginUserContextHolder
import dev.usbharu.hideout.domain.mastodon.model.generated.AccountSource
import dev.usbharu.hideout.domain.mastodon.model.generated.CredentialAccount
import dev.usbharu.hideout.domain.mastodon.model.generated.Role
import dev.usbharu.hideout.mastodon.service.account.AccountApiService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import utils.TestTransaction

@ExtendWith(MockitoExtension::class)
class MastodonAccountApiControllerTest {

    private lateinit var mockMvc: MockMvc

    @Spy
    private val loginUserContextHolder = OAuth2JwtLoginUserContextHolder()

    @Spy
    private lateinit var testTransaction: TestTransaction

    @Mock
    private lateinit var accountApiService: AccountApiService

    @InjectMocks
    private lateinit var mastodonAccountApiController: MastodonAccountApiController

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mastodonAccountApiController).build()
    }

    @Test
    fun `apiV1AccountsVerifyCredentialsGet JWTで認証時に200が返ってくる`() = runTest {

        val createEmptyContext = SecurityContextHolder.createEmptyContext()
        createEmptyContext.authentication = JwtAuthenticationToken(
            Jwt.withTokenValue("a").header("alg", "RS236").claim("uid", "1234").build()
        )
        SecurityContextHolder.setContext(createEmptyContext)
        val credentialAccount = CredentialAccount(
            id = "",
            username = "",
            acct = "",
            url = "",
            displayName = "",
            note = "",
            avatar = "",
            avatarStatic = "",
            header = "",
            headerStatic = "",
            locked = false,
            fields = emptyList(),
            emojis = emptyList(),
            bot = false,
            group = false,
            discoverable = true,
            createdAt = "",
            lastStatusAt = "",
            statusesCount = 0,
            followersCount = 0,
            source = AccountSource(
                note = "",
                fields = emptyList(),
                privacy = AccountSource.Privacy.public,
                sensitive = false,
                followRequestsCount = 0
            ),
            noindex = false,
            moved = false,
            suspendex = false,
            limited = false,
            followingCount = 0,
            role = Role(0, "ADMIN", "", 0, false)
        )
        whenever(accountApiService.verifyCredentials(eq(1234))).doReturn(credentialAccount)

        val objectMapper = ActivityPubConfig().objectMapper()

        mockMvc
            .get("/api/v1/accounts/verify_credentials")
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(credentialAccount)) } }
    }

    @Test
    fun `apiV1AccountsVerifyCredentialsGet POSTは405が返ってくる`() {
        mockMvc.post("/api/v1/accounts/verify_credentials")
            .andExpect { status { isMethodNotAllowed() } }
    }

    @Test
    fun `apiV1AccountsPost GETは405が返ってくる`() {
        mockMvc.get("/api/v1/accounts")
            .andExpect { status { isMethodNotAllowed() } }
    }

    @Test
    fun `apiV1AccountsPost アカウント作成成功時302とアカウントのurlが返ってくる`() {
        mockMvc
            .post("/api/v1/accounts") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("username", "hoge")
                param("password", "very_secure_password")
                param("email", "email@example.com")
                param("agreement", "true")
                param("locale", "true")
            }.asyncDispatch()
            .andExpect { header { string("location", "/users/hoge") } }
            .andExpect { status { isFound() } }
    }

    @Test
    fun `apiV1AccountsIdFollowPost フォロー成功時は200が返ってくる`() {
        val createEmptyContext = SecurityContextHolder.createEmptyContext()
        createEmptyContext.authentication = JwtAuthenticationToken(
            Jwt.withTokenValue("a").header("alg", "RS236").claim("uid", "1234").build()
        )
        SecurityContextHolder.setContext(createEmptyContext)
        mockMvc
            .post("/api/v1/accounts/1/follow") {
                contentType = MediaType.APPLICATION_JSON
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }

    }
}
