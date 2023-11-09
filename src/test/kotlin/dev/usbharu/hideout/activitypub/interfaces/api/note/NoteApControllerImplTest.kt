package dev.usbharu.hideout.activitypub.interfaces.api.note

import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.service.objects.note.NoteApApiService
import dev.usbharu.hideout.application.config.ActivityPubConfig
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureUser
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.FilterChainProxy
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.security.web.util.matcher.AnyRequestMatcher
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import java.net.URL

@ExtendWith(MockitoExtension::class)
class NoteApControllerImplTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var noteApApiService: NoteApApiService

    @InjectMocks
    private lateinit var noteApControllerImpl: NoteApControllerImpl

    @BeforeEach
    fun setUp() {

        mockMvc = MockMvcBuilders.standaloneSetup(noteApControllerImpl)
            .apply<StandaloneMockMvcBuilder>(
                springSecurity(
                    FilterChainProxy(
                        DefaultSecurityFilterChain(
                            AnyRequestMatcher.INSTANCE
                        )
                    )
                )
            )
            .build()
    }

    @Test
    fun `postAP 匿名で取得できる`() = runTest {

        val note = Note(
            name = "Note",
            id = "https://example.com/users/hoge/posts/1234",
            attributedTo = "https://example.com/users/hoge",
            content = "Hello",
            published = "2023-11-02T15:30:34.160Z"
        )
        whenever(noteApApiService.getNote(eq(1234), isNull())).doReturn(
            note
        )

        val objectMapper = ActivityPubConfig().objectMapper()

        mockMvc
            .get("/users/hoge/posts/1234") {
                with(anonymous())
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(note)) } }
    }

    @Test
    fun `postAP 存在しない場合は404`() = runTest {
        whenever(noteApApiService.getNote(eq(123), isNull())).doReturn(null)

        mockMvc
            .get("/users/hoge/posts/123") {
                with(anonymous())
            }
            .asyncDispatch()
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `postAP 認証に成功している場合userIdがnullでない`() = runTest {
        val note = Note(
            name = "Note",
            id = "https://example.com/users/hoge/posts/1234",
            attributedTo = "https://example.com/users/hoge",
            content = "Hello",
            published = "2023-11-02T15:30:34.160Z"
        )
        whenever(noteApApiService.getNote(eq(1234), isNotNull())).doReturn(note)

        val objectMapper = ActivityPubConfig().objectMapper()

        val preAuthenticatedAuthenticationToken = PreAuthenticatedAuthenticationToken(
            "", HttpRequest(
                URL("https://follower.example.com"),
                HttpHeaders(
                    mapOf()
                ), HttpMethod.GET
            )
        ).apply { details = HttpSignatureUser("fuga", "follower.example.com", 123, true, true, mutableListOf()) }
        SecurityContextHolder.getContext().authentication = preAuthenticatedAuthenticationToken

        mockMvc.get("/users/hoge/posts/1234") {
            with(
                authentication(
                    preAuthenticatedAuthenticationToken
                )
            )
        }.asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(note)) } }
    }
}
