/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package activitypub.note

import dev.usbharu.hideout.SpringApplication
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
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
import util.WithHttpSignature
import util.WithMockHttpSignature

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

    @Test
    @Sql("/sql/note/httpSignature認証でフォロワーがpublic投稿を取得できる.sql")
    @WithHttpSignature(keyId = "https://follower.example.com/users/test-user5#pubkey")
    fun HttpSignature認証でフォロワーがpublic投稿を取得できる() {
        mockMvc
            .get("/users/test-user4/posts/1237") {
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
    @Sql("/sql/note/httpSignature認証でフォロワーがunlisted投稿を取得できる.sql")
    @WithHttpSignature(keyId = "https://follower.example.com/users/test-user7#pubkey")
    fun httpSignature認証でフォロワーがunlisted投稿を取得できる() {
        mockMvc
            .get("/users/test-user6/posts/1238") {
                accept(MediaType("application", "activity+json"))
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andExpect { content { contentType("application/activity+json") } }
            .andExpect { jsonPath("\$.type") { value("Note") } }
            .andExpect { jsonPath("\$.to") { value("https://example.com/users/test-user6/followers") } }
            .andExpect { jsonPath("\$.cc") { value("https://www.w3.org/ns/activitystreams#Public") } }
    }

    @Test
    @Sql("/sql/note/httpSignature認証でフォロワーがfollowers投稿を取得できる.sql")
    @WithHttpSignature(keyId = "https://follower.example.com/users/test-user9#pubkey")
    fun httpSignature認証でフォロワーがfollowers投稿を取得できる() {
        mockMvc
            .get("/users/test-user8/posts/1239") {
                accept(MediaType("application", "activity+json"))
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andExpect { content { contentType("application/activity+json") } }
            .andExpect { jsonPath("\$.type") { value("Note") } }
            .andExpect { jsonPath("\$.to") { value("https://example.com/users/test-user8/followers") } }
            .andExpect { jsonPath("\$.cc") { value("https://example.com/users/test-user8/followers") } }

    }

    @Test
    @Sql("/sql/note/リプライになっている投稿はinReplyToが存在する.sql")
    @WithMockHttpSignature
    fun リプライになっている投稿はinReplyToが存在する() {
        mockMvc
            .get("/users/test-user10/posts/1241") {
                accept(MediaType("application", "activity+json"))
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andExpect { content { contentType("application/activity+json") } }
            .andExpect { jsonPath("\$.type") { value("Note") } }
            .andExpect { jsonPath("\$.inReplyTo") { value("https://example.com/users/test-user10/posts/1240") } }
    }

    @Test
    @Sql("/sql/note/メディア付き投稿はattachmentにDocumentとして画像が存在する.sql")
    @WithMockHttpSignature
    fun メディア付き投稿はattachmentにDocumentとして画像が存在する() {
        mockMvc
            .get("/users/test-user10/posts/1242") {
                accept(MediaType("application", "activity+json"))
            }
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andExpect { content { contentType("application/activity+json") } }
            .andExpect { jsonPath("\$.type") { value("Note") } }
            .andExpect { jsonPath("\$.attachment") { isArray() } }
            .andExpect { jsonPath("\$.attachment[0].type") { value("Document") } }
            .andExpect { jsonPath("\$.attachment[0].url") { value("https://example.com/media/test-media.png") } }
            .andExpect { jsonPath("\$.attachment[1].type") { value("Document") } }
            .andExpect { jsonPath("\$.attachment[1].url") { value("https://example.com/media/test-media2.png") } }
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
