package dev.usbharu.hideout.mastodon.interfaces.api.timeline

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.core.infrastructure.springframework.security.OAuth2JwtLoginUserContextHolder
import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.service.timeline.TimelineApiService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.net.URL

@ExtendWith(MockitoExtension::class)
class MastodonTimelineApiControllerTest {

    @Spy
    private val loginUserContextHolder = OAuth2JwtLoginUserContextHolder()

    @Mock
    private lateinit var timelineApiService: TimelineApiService

    @Spy
    private val applicationConfig: ApplicationConfig = ApplicationConfig(URL("https://example.com"))

    @InjectMocks
    private lateinit var mastodonTimelineApiController: MastodonTimelineApiController

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mastodonTimelineApiController).build()
    }

    val statusList = PaginationList<Status, Long>(
        listOf<Status>(
            Status(
                id = "",
                uri = "",
                createdAt = "",
                account = Account(
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
                    noindex = false,
                    moved = false,
                    suspendex = false,
                    limited = false,
                    followingCount = 0
                ),
                content = "",
                visibility = Status.Visibility.public,
                sensitive = false,
                spoilerText = "",
                mediaAttachments = emptyList(),
                mentions = emptyList(),
                tags = emptyList(),
                emojis = emptyList(),
                reblogsCount = 0,
                favouritesCount = 0,
                repliesCount = 0,
                url = "https://example.com",
                inReplyToId = null,
                inReplyToAccountId = null,
                language = "ja_JP",
                text = "Test",
                editedAt = null

            ),
            Status(
                id = "",
                uri = "",
                createdAt = "",
                account = Account(
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
                    noindex = false,
                    moved = false,
                    suspendex = false,
                    limited = false,
                    followingCount = 0
                ),
                content = "",
                visibility = Status.Visibility.public,
                sensitive = false,
                spoilerText = "",
                mediaAttachments = emptyList(),
                mentions = emptyList(),
                tags = emptyList(),
                emojis = emptyList(),
                reblogsCount = 0,
                favouritesCount = 0,
                repliesCount = 0,
                url = "https://example.com",
                inReplyToId = null,
                inReplyToAccountId = null,
                language = "ja_JP",
                text = "Test",
                editedAt = null

            )
        ), null, null
    )

    @Test
    fun `apiV1TimelineHogeGet JWT認証でログインじ200が返ってくる`() = runTest {

        val createEmptyContext = SecurityContextHolder.createEmptyContext()
        createEmptyContext.authentication = JwtAuthenticationToken(
            Jwt.withTokenValue("a").header("alg", "RS236").claim("uid", "1234").build()
        )
        SecurityContextHolder.setContext(createEmptyContext)

        whenever(
            timelineApiService.homeTimeline(
                eq(1234),
                any()
            )
        ).doReturn(statusList)

        val objectMapper = jacksonObjectMapper()

        mockMvc
            .get("/api/v1/timelines/home?max_id=123456&since_id=1234567&min_id=54321&limit=20")
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(statusList)) } }
    }

    @Test
    fun `apiV1TimelineHomeGet パラメーターがなくても取得できる`() = runTest {
        val createEmptyContext = SecurityContextHolder.createEmptyContext()
        createEmptyContext.authentication = JwtAuthenticationToken(
            Jwt.withTokenValue("a").header("alg", "RS236").claim("uid", "1234").build()
        )
        SecurityContextHolder.setContext(createEmptyContext)

        whenever(
            timelineApiService.homeTimeline(
                eq(1234),
                any()
            )
        ).doReturn(statusList)

        val objectMapper = jacksonObjectMapper()

        mockMvc
            .get("/api/v1/timelines/home")
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(statusList)) } }
    }

    @Test
    fun `apiV1TimelineHomeGet POSTには405を返す`() {
        mockMvc
            .post("/api/v1/timelines/home?max_id=123456&since_id=1234567&min_id=54321&limit=20")
            .andExpect { status { isMethodNotAllowed() } }
    }

    @Test
    fun `apiV1TimelinePublicGet GETで200が返ってくる`() = runTest {
        whenever(
            timelineApiService.publicTimeline(
                localOnly = eq(false),
                remoteOnly = eq(true),
                mediaOnly = eq(false),
                any()
            )
        ).doAnswer {
            println(it.arguments.joinToString())
            statusList
        }

        val objectMapper = jacksonObjectMapper()

        mockMvc
            .get("/api/v1/timelines/public?local=false&remote=true&only_media=false&max_id=1234&since_id=12345&min_id=4321&limit=20")
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(statusList)) } }
    }

    @Test
    fun `apiV1TimelinePublicGet POSTで405が返ってくる`() {
        mockMvc.post("/api/v1/timelines/public")
            .andExpect { status { isMethodNotAllowed() } }
    }

    @Test
    fun `apiV1TimelinePublicGet パラメーターがなくても取得できる`() = runTest {
        whenever(
            timelineApiService.publicTimeline(
                localOnly = eq(false),
                remoteOnly = eq(false),
                mediaOnly = eq(false),
                any()
            )
        ).doAnswer {
            println(it.arguments.joinToString())
            statusList
        }

        val objectMapper = jacksonObjectMapper()

        mockMvc
            .get("/api/v1/timelines/public")
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(statusList)) } }
    }
}
