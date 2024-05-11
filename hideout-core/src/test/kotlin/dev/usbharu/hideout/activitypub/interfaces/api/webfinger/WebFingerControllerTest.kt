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

package dev.usbharu.hideout.activitypub.interfaces.api.webfinger

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.webfinger.WebFinger
import dev.usbharu.hideout.activitypub.service.webfinger.WebFingerApiService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import utils.UserBuilder

@ExtendWith(MockitoExtension::class)
class WebFingerControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var webFingerApiService: WebFingerApiService

    @Mock
    private lateinit var applicationConfig: ApplicationConfig

    @InjectMocks
    private lateinit var webFingerController: WebFingerController

    @BeforeEach
    fun setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(webFingerController).build()
    }

    @Test
    fun `webfinger 存在するacctを指定したとき200 OKでWebFingerのレスポンスが返ってくる`() = runTest {

        val user = UserBuilder.localUserOf()
        whenever(
            webFingerApiService.findByNameAndDomain(
                eq("hoge"),
                eq("example.com")
            )
        ).doReturn(user)

        val contentAsString = mockMvc.perform(get("/.well-known/webfinger?resource=acct:hoge@example.com"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .response
            .contentAsString

        val objectMapper = jacksonObjectMapper()

        val readValue = objectMapper.readValue<WebFinger>(contentAsString)

        val expected = WebFinger(
            subject = "acct:${user.name}@${user.domain}",
            listOf(
                WebFinger.Link(
                    "self",
                    "application/activity+json",
                    user.url
                )
            )
        )

        assertThat(readValue).isEqualTo(expected)
    }

    @Test
    fun `webfinger 存在しないacctを指定したとき404 Not Foundが返ってくる`() = runTest {
        whenever(
            webFingerApiService.findByNameAndDomain(
                eq("fuga"),
                eq("example.com")
            )
        ).doThrow(UserNotFoundException::class)

        mockMvc.perform(get("/.well-known/webfinger?resource=acct:fuga@example.com"))
            .andDo(print())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `webfinger acctとして解釈できない場合は400 Bad Requestが返ってくる`() {
        mockMvc.perform(get("/.well-known/webfinger?resource=@hello@aa@aab@aaa"))
            .andDo(print())
            .andExpect(status().isBadRequest)
    }
}
