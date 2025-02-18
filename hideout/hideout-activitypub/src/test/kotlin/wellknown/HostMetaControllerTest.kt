package wellknown

import dev.usbharu.hideout.SpringApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
class HostMetaControllerTest {

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
    fun hostmeta() {
        mockMvc
            .get("/.well-known/host-meta") {
                accept(MediaType.APPLICATION_XML)
            }
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andExpect { content { contentType(MediaType("application", "xrd+xml")) } }
    }

    @Test
    fun hostmetaJson() {
        mockMvc
            .get("/.well-known/host-meta") {
                accept(MediaType.APPLICATION_JSON)
            }
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andExpect { content { contentType(MediaType("application", "json")) } }
    }

    @Test
    fun hostmetaJson2() {
        mockMvc
            .get("/.well-known/host-meta.json") {
                accept(MediaType.APPLICATION_JSON)
            }
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andExpect { content { contentType(MediaType("application", "json")) } }
    }
}