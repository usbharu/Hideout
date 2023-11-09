package dev.usbharu.hideout.mastodon.interfaces.api.apps

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.domain.mastodon.model.generated.Application
import dev.usbharu.hideout.domain.mastodon.model.generated.AppsRequest
import dev.usbharu.hideout.generate.JsonOrFormModelMethodProcessor
import dev.usbharu.hideout.mastodon.service.app.AppApiService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.method.annotation.ModelAttributeMethodProcessor
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor

@ExtendWith(MockitoExtension::class)
class MastodonAppsApiControllerTest {

    @Mock
    private lateinit var appApiService: AppApiService

    @InjectMocks
    private lateinit var mastodonAppsApiController: MastodonAppsApiController

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mastodonAppsApiController).setCustomArgumentResolvers(
            JsonOrFormModelMethodProcessor(
                ModelAttributeMethodProcessor(false), RequestResponseBodyMethodProcessor(
                    mutableListOf<HttpMessageConverter<*>>(
                        MappingJackson2HttpMessageConverter()
                    )
                )
            )
        ).build()
    }

    @Test
    fun `apiV1AppsPost JSONで作成に成功したら200が返ってくる`() = runTest {

        val appsRequest = AppsRequest(
            "test",
            "https://example.com",
            "write",
            null
        )
        val application = Application(
            "test",
            "",
            null,
            "safdash;",
            "aksdhgoa"
        )

        whenever(appApiService.createApp(eq(appsRequest))).doReturn(application)

        val objectMapper = jacksonObjectMapper()
        val writeValueAsString = objectMapper.writeValueAsString(appsRequest)

        mockMvc
            .post("/api/v1/apps") {
                contentType = MediaType.APPLICATION_JSON
                content = writeValueAsString
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(application)) } }
    }

    @Test
    fun `apiV1AppsPost FORMで作成に成功したら200が返ってくる`() = runTest {

        val appsRequest = AppsRequest(
            "test",
            "https://example.com",
            "write",
            null
        )
        val application = Application(
            "test",
            "",
            null,
            "safdash;",
            "aksdhgoa"
        )

        whenever(appApiService.createApp(eq(appsRequest))).doReturn(application)

        val objectMapper = jacksonObjectMapper()

        mockMvc
            .post("/api/v1/apps") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("client_name", "test")
                param("redirect_uris", "https://example.com")
                param("scopes", "write")
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(application)) } }
    }
}
