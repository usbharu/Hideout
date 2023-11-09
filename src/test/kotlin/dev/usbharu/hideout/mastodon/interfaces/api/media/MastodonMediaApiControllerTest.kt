package dev.usbharu.hideout.mastodon.interfaces.api.media

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment
import dev.usbharu.hideout.mastodon.service.media.MediaApiService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class MastodonMediaApiControllerTest {

    @Mock
    private lateinit var mediaApiService: MediaApiService

    @InjectMocks
    private lateinit var mastodonMediaApiController: MastodonMediaApiController

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mastodonMediaApiController).build()
    }

    @Test
    fun `apiV1MediaPost ファイルとサムネイルをアップロードできる`() = runTest {

        val mediaAttachment = MediaAttachment(
            id = "1234",
            type = MediaAttachment.Type.image,
            url = "https://example.com",
            previewUrl = "https://example.com",
            remoteUrl = "https://example.com",
            description = "pngImageStream",
            blurhash = "",
            textUrl = "https://example.com"
        )
        whenever(mediaApiService.postMedia(any())).doReturn(mediaAttachment)

        val objectMapper = jacksonObjectMapper()

        mockMvc
            .multipart("/api/v1/media") {
                file(MockMultipartFile("file", "test.png", "image/png", "jpgImageStream".toByteArray()))
                file(MockMultipartFile("thumbnail", "thumbnail.png", "image/png", "pngImageStream".toByteArray()))
                param("description", "jpgImage")
                param("focus", "")
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(mediaAttachment)) } }
    }

    @Test
    fun `apiV1MediaPost ファイルだけをアップロードできる`() = runTest {

        val mediaAttachment = MediaAttachment(
            id = "1234",
            type = MediaAttachment.Type.image,
            url = "https://example.com",
            previewUrl = "https://example.com",
            remoteUrl = "https://example.com",
            description = "pngImageStream",
            blurhash = "",
            textUrl = "https://example.com"
        )
        whenever(mediaApiService.postMedia(any())).doReturn(mediaAttachment)

        val objectMapper = jacksonObjectMapper()

        mockMvc
            .multipart("/api/v1/media") {
                file(MockMultipartFile("file", "test.png", "image/png", "jpgImageStream".toByteArray()))
                param("description", "jpgImage")
                param("focus", "")
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(mediaAttachment)) } }
    }
}
