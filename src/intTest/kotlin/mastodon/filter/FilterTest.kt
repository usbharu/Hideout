package mastodon.filter

import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.application.config.ActivityPubConfig
import dev.usbharu.hideout.domain.mastodon.model.generated.FilterPostRequest
import dev.usbharu.hideout.domain.mastodon.model.generated.FilterPostRequestKeyword
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/test-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class FilterTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @Test
    fun `apiV2FiltersPost write権限で追加できる`() {
        mockMvc
            .post("/api/v2/filters") {
                contentType = MediaType.APPLICATION_JSON
                content = ActivityPubConfig().objectMapper().writeValueAsString(
                    FilterPostRequest(
                        title = "mute test",
                        context = listOf(FilterPostRequest.Context.home, FilterPostRequest.Context.public),
                        filterAction = FilterPostRequest.FilterAction.warn,
                        expiresIn = null,
                        keywordsAttributes = listOf(
                            FilterPostRequestKeyword(
                                keyword = "hoge",
                                wholeWord = false,
                                regex = true
                            )
                        )
                    )
                )
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect {
                content {
                    jsonPath("$.keywords[0].keyword") {
                        value("hoge")
                    }
                }
            }
    }

    @Test
    fun `apiV2FiltersPost write_filters権限で追加できる`() {
        mockMvc
            .post("/api/v2/filters") {
                contentType = MediaType.APPLICATION_JSON
                content = ActivityPubConfig().objectMapper().writeValueAsString(
                    FilterPostRequest(
                        title = "mute test",
                        context = listOf(FilterPostRequest.Context.home, FilterPostRequest.Context.public),
                        filterAction = FilterPostRequest.FilterAction.warn,
                        expiresIn = null,
                        keywordsAttributes = listOf(
                            FilterPostRequestKeyword(
                                keyword = "fuga",
                                wholeWord = true,
                                regex = false
                            )
                        )
                    )
                )
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect {
                content {
                    jsonPath("$.keywords[0].keyword") {
                        value("fuga")
                    }
                }
            }
    }

    @Test
    fun `apiV2FiltersPost read権限で401`() {
        mockMvc
            .post("/api/v2/filters") {
                contentType = MediaType.APPLICATION_JSON
                content = ActivityPubConfig().objectMapper().writeValueAsString(
                    FilterPostRequest(
                        title = "mute test",
                        context = listOf(FilterPostRequest.Context.home, FilterPostRequest.Context.public),
                        filterAction = FilterPostRequest.FilterAction.warn,
                        expiresIn = null,
                        keywordsAttributes = listOf(
                            FilterPostRequestKeyword(
                                keyword = "fuga",
                                wholeWord = true,
                                regex = false
                            )
                        )
                    )
                )
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `apiV2FiltersGet read権限で取得できる`() {
        mockMvc
            .get("/api/v2/filters") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersGet read_filters権限で取得できる`() {
        mockMvc
            .get("/api/v2/filters") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read:filters"))
                )
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
    }

    @Test
    fun `apiV2FiltersGet write権限で401`() {
        mockMvc
            .get("/api/v2/filters") {
                with(
                    SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_write"))
                )
            }
            .andExpect { status { isForbidden() } }
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun dropDatabase(@Autowired flyway: Flyway) {
            flyway.clean()
            flyway.migrate()
        }
    }
}