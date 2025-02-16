package activity

import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.activitypub.interfaces.api.APActorController
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class, APActorController::class])
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/actors.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class APActorControllerTest {

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
    fun user() {
        mockMvc
            .get("/users/test") {
                accept(MediaType("application", "activity+json"))
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }

    }
}