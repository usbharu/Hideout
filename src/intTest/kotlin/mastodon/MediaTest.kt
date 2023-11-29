package mastodon

import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.core.service.media.MediaDataStore
import dev.usbharu.hideout.core.service.media.MediaSaveRequest
import dev.usbharu.hideout.core.service.media.SuccessSavedMedia
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/test-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class MediaTest {

    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc


    @MockBean
    private lateinit var mediaDataStore: MediaDataStore

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @Test
    fun メディアをアップロードできる() = runTest {
        whenever(mediaDataStore.save(any<MediaSaveRequest>())).doReturn(SuccessSavedMedia("", "", ""))

        mockMvc
            .multipart("/api/v1/media") {

                file(
                    MockMultipartFile(
                        "file",
                        "400x400.png",
                        "image/png",
                        String.javaClass.classLoader.getResourceAsStream("media/400x400.png")
                    )
                )
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write")))
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }
}
