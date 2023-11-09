package dev.usbharu.hideout.mastodon.interfaces.api.instance

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.domain.mastodon.model.generated.*
import dev.usbharu.hideout.mastodon.service.instance.InstanceApiService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class MastodonInstanceApiControllerTest {

    @Mock
    private lateinit var instanceApiService: InstanceApiService

    @InjectMocks
    private lateinit var mastodonInstanceApiController: MastodonInstanceApiController

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mastodonInstanceApiController).build()
    }

    @Test
    fun `apiV1InstanceGet GETしたら200が返ってくる`() = runTest {

        val v1Instance = V1Instance(
            uri = "https://example.com",
            title = "hideout",
            shortDescription = "test",
            description = "test instance",
            email = "test@example.com",
            version = "0.0.1",
            urls = V1InstanceUrls(streamingApi = "https://example.com/atreaming"),
            stats = V1InstanceStats(userCount = 1, statusCount = 0, domainCount = 0),
            thumbnail = "https://example.com",
            languages = emptyList(),
            registrations = false,
            approvalRequired = false,
            invitesEnabled = false,
            configuration = V1InstanceConfiguration(
                accounts = V1InstanceConfigurationAccounts(0),
                V1InstanceConfigurationStatuses(100, 4, 23),
                V1InstanceConfigurationMediaAttachments(emptyList(), 100, 100, 100, 100, 100),
                V1InstanceConfigurationPolls(
                    10, 10, 10, 10
                )
            ),
            contactAccount = Account(
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
            emptyList()
        )
        whenever(instanceApiService.v1Instance()).doReturn(v1Instance)

        val objectMapper = jacksonObjectMapper()

        mockMvc
            .get("/api/v1/instance")
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(objectMapper)) } }
    }

    @Test
    fun `apiV1InstanceGet POSTしたら405が返ってくる`() {
        mockMvc
            .post("/api/v1/instance")
            .andExpect { status { isMethodNotAllowed() } }
    }
}
