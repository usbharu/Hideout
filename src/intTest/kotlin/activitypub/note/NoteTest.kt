package activitypub.note

import dev.usbharu.hideout.SpringApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
class NoteTest {
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply<DefaultMockMvcBuilder>(springSecurity()).build()
    }

    @Test
    @WithAnonymousUser
    @Sql("/sql/note/匿名でpublic投稿を取得できる.sql")
    fun `匿名でpublic投稿を取得できる`() {
        mockMvc
            .get("/users/test-user/posts/1234") {
                accept(MediaType("application", "activity+json"))
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andExpect { content { contentType("application/activity+json") } }
            .andExpect { jsonPath("\$.type") { value("Note") } }
            .andExpect { jsonPath("\$.to") { value("https://www.w3.org/ns/activitystreams#Public") } }
            .andExpect { jsonPath("\$.cc") { value("https://www.w3.org/ns/activitystreams#Public") } }
    }

    @Test
    @Sql("/sql/note/匿名でunlisted投稿を取得できる.sql")
    @WithAnonymousUser
    fun 匿名でunlisted投稿を取得できる() {
        mockMvc
            .get("/users/test-user2/posts/1235") {
                accept(MediaType("application", "activity+json"))
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andExpect { content { contentType("application/activity+json") } }
            .andExpect { jsonPath("\$.type") { value("Note") } }
            .andExpect { jsonPath("\$.to") { value("https://example.com/users/test-user2/followers") } }
            .andExpect { jsonPath("\$.cc") { value("https://www.w3.org/ns/activitystreams#Public") } }
    }

    @Test
    @Transactional
    @WithAnonymousUser
    @Sql("/sql/note/匿名でfollowers投稿を取得しようとすると404.sql")
    fun 匿名でfollowers投稿を取得しようとすると404() {
        mockMvc
            .get("/users/test-user2/posts/1236") {
                accept(MediaType("application", "activity+json"))
            }
            .asyncDispatch()
            .andExpect { status { isNotFound() } }
    }

    @Test
    @WithAnonymousUser
    fun 匿名でdirect投稿を取得しようとすると404() {
        mockMvc
            .get("/users/test-user2/posts/1236") {
                accept(MediaType("application", "activity+json"))
            }
            .asyncDispatch()
            .andExpect { status { isNotFound() } }
    }
}
