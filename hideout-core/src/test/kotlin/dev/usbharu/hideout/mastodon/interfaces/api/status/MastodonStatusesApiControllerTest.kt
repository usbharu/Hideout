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

package dev.usbharu.hideout.mastodon.interfaces.api.status

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.core.infrastructure.springframework.security.OAuth2JwtLoginUserContextHolder
import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.generate.JsonOrFormModelMethodProcessor
import dev.usbharu.hideout.mastodon.service.status.StatusesApiService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.method.annotation.ModelAttributeMethodProcessor
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor

@ExtendWith(MockitoExtension::class)
class MastodonStatusesApiControllerTest {

    @Spy
    private val loginUserContextHolder = OAuth2JwtLoginUserContextHolder()

    @Mock
    private lateinit var statusesApiService: StatusesApiService

    @InjectMocks
    private lateinit var mastodonStatusesApiController: MastodonStatusesApiContoller

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mastodonStatusesApiController).setCustomArgumentResolvers(
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
    fun `apiV1StatusesPost JWT認証時POSTすると投稿できる`() = runTest {
        val createEmptyContext = SecurityContextHolder.createEmptyContext()
        createEmptyContext.authentication = JwtAuthenticationToken(
            Jwt.withTokenValue("a").header("alg", "RS236").claim("uid", "1234").build()
        )
        SecurityContextHolder.setContext(createEmptyContext)
        val status = Status(
            id = "",
            uri = "",
            createdAt = "",
            account = Account(
                id = "",
                username = "",
                acct = "",
                url = "",
                displayName = "",
                note = "",
                avatar = "",
                avatarStatic = "",
                header = "",
                headerStatic = "",
                locked = false,
                fields = emptyList(),
                emojis = emptyList(),
                bot = false,
                group = false,
                discoverable = true,
                createdAt = "",
                lastStatusAt = "",
                statusesCount = 0,
                followersCount = 0,
                noindex = false,
                moved = false,
                suspendex = false,
                limited = false,
                followingCount = 0
            ),
            content = "",
            visibility = Status.Visibility.public,
            sensitive = false,
            spoilerText = "",
            mediaAttachments = emptyList(),
            mentions = emptyList(),
            tags = emptyList(),
            emojis = emptyList(),
            reblogsCount = 0,
            favouritesCount = 0,
            repliesCount = 0,
            url = "https://example.com",
            inReplyToId = null,
            inReplyToAccountId = null,
            language = "ja_JP",
            text = "Test",
            editedAt = null

        )

        val objectMapper = jacksonObjectMapper()

        val statusesRequest = StatusesRequest()

        statusesRequest.status = "hello"

        whenever(statusesApiService.postStatus(eq(statusesRequest), eq(1234))).doReturn(status)

        mockMvc
            .post("/api/v1/statuses") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(statusesRequest)
            }
            .asyncDispatch()
            .andExpect { status { isOk() } }
            .andExpect { content { json(objectMapper.writeValueAsString(status)) } }
    }
}
