package dev.usbharu.hideout.activitypub.interfaces.api.inbox

import dev.usbharu.hideout.activitypub.domain.exception.JsonParseException
import dev.usbharu.hideout.activitypub.service.common.APService
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class InboxControllerImplTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var apService: APService

    @InjectMocks
    private lateinit var inboxController: InboxControllerImpl

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inboxController).build()
    }

    @Test
    fun `inbox 正常なPOSTリクエストをしたときAcceptが返ってくる`() = runTest {


        val json = """{"type":"Follow"}"""
        whenever(apService.parseActivity(eq(json))).doReturn(ActivityType.Follow)
        whenever(
            apService.processActivity(
                eq(json),
                eq(ActivityType.Follow),
                any(),
                any()

            )
        ).doReturn(Unit)

        mockMvc
            .post("/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
            }
            .asyncDispatch()
            .andExpect {
                status { isAccepted() }
            }

    }

    @Test
    fun `inbox parseActivityに失敗したときAcceptが返ってくる`() = runTest {
        val json = """{"type":"Hoge"}"""
        whenever(apService.parseActivity(eq(json))).doThrow(JsonParseException::class)

        mockMvc
            .post("/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
            }
            .asyncDispatch()
            .andExpect {
                status { isAccepted() }
            }

    }

    @Test
    fun `inbox processActivityに失敗したときAcceptが返ってくる`() = runTest {
        val json = """{"type":"Follow"}"""
        whenever(apService.parseActivity(eq(json))).doReturn(ActivityType.Follow)
        whenever(
            apService.processActivity(
                eq(json),
                eq(ActivityType.Follow),
                any(),
                any()
            )
        ).doThrow(FailedToGetResourcesException::class)

        mockMvc
            .post("/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
            }
            .asyncDispatch()
            .andExpect {
                status { isAccepted() }
            }

    }

    @Test
    fun `inbox GETリクエストには405を返す`() {
        mockMvc.get("/inbox").andExpect { status { isMethodNotAllowed() } }
    }

    @Test
    fun `user-inbox 正常なPOSTリクエストをしたときAcceptが返ってくる`() = runTest {


        val json = """{"type":"Follow"}"""
        whenever(apService.parseActivity(eq(json))).doReturn(ActivityType.Follow)
        whenever(apService.processActivity(eq(json), eq(ActivityType.Follow), any(), any())).doReturn(
            Unit
        )

        mockMvc
            .post("/users/hoge/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
            }
            .asyncDispatch()
            .andExpect {
                status { isAccepted() }
            }

    }

    @Test
    fun `user-inbox parseActivityに失敗したときAcceptが返ってくる`() = runTest {
        val json = """{"type":"Hoge"}"""
        whenever(apService.parseActivity(eq(json))).doThrow(JsonParseException::class)

        mockMvc
            .post("/users/hoge/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
            }
            .asyncDispatch()
            .andExpect {
                status { isAccepted() }
            }

    }

    @Test
    fun `user-inbox processActivityに失敗したときAcceptが返ってくる`() = runTest {
        val json = """{"type":"Follow"}"""
        whenever(apService.parseActivity(eq(json))).doReturn(ActivityType.Follow)
        whenever(
            apService.processActivity(
                eq(json),
                eq(ActivityType.Follow),
                any(),
                any()
            )
        ).doThrow(FailedToGetResourcesException::class)

        mockMvc
            .post("/users/hoge/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
            }
            .asyncDispatch()
            .andExpect {
                status { isAccepted() }
            }

    }

    @Test
    fun `user-inbox GETリクエストには405を返す`() {
        mockMvc.get("/users/hoge/inbox").andExpect { status { isMethodNotAllowed() } }
    }
}
