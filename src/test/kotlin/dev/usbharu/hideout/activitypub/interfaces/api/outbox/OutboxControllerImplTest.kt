package dev.usbharu.hideout.activitypub.interfaces.api.outbox

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class OutboxControllerImplTest {

    private lateinit var mockMvc: MockMvc

    @InjectMocks
    private lateinit var outboxController: OutboxControllerImpl

    @BeforeEach
    fun setUp() {
        mockMvc =
            MockMvcBuilders.standaloneSetup(outboxController).build()
    }

    @Test
    fun `outbox GETに501を返す`() {
        mockMvc
            .get("/outbox")
            .andDo { print() }
            .andExpect { status { isNotImplemented() } }
    }

    @Test
    fun `user-outbox GETに501を返す`() {
        mockMvc
            .get("/users/hoge/outbox")
            .andDo { print() }
            .andExpect { status { isNotImplemented() } }
    }

    @Test
    fun `outbox POSTに501を返す`() {
        mockMvc
            .post("/outbox")
            .andDo { print() }
            .andExpect { status { isNotImplemented() } }
    }

    @Test
    fun `user-outbox POSTに501を返す`() {
        mockMvc
            .post("/users/hoge/outbox")
            .andDo { print() }
            .andExpect { status { isNotImplemented() } }
    }
}
