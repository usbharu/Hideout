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

package dev.usbharu.hideout.activitypub.interfaces.api.actor

import dev.usbharu.hideout.activitypub.domain.model.Image
import dev.usbharu.hideout.activitypub.domain.model.Key
import dev.usbharu.hideout.activitypub.domain.model.Person
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.config.ActivityPubConfig
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import kotlinx.coroutines.test.runTest
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class ActorAPControllerImplTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var apUserService: APUserService

    @InjectMocks
    private lateinit var userAPControllerImpl: UserAPControllerImpl

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(userAPControllerImpl)
            .setMessageConverters(MappingJackson2HttpMessageConverter(ActivityPubConfig().objectMapper()))
            .build()
    }

    @Test
    fun `userAp 存在するユーザーにGETするとPersonが返ってくる`(): Unit = runTest {
        val person = Person(
            name = "Hoge",
            id = "https://example.com/users/hoge",
            preferredUsername = "hoge",
            summary = "fuga",
            inbox = "https://example.com/users/hoge/inbox",
            outbox = "https://example.com/users/hoge/outbox",
            url = "https://example.com/users/hoge",
            icon = Image(
                mediaType = "image/jpeg",
                url = "https://example.com/users/hoge/icon.jpg"
            ),
            publicKey = Key(
                id = "https://example.com/users/hoge#pubkey",
                owner = "https://example.com/users/hoge",
                publicKeyPem = "-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----"
            ),
            endpoints = mapOf("sharedInbox" to "https://example.com/inbox"),
            followers = "https://example.com/users/hoge/followers",
            following = "https://example.com/users/hoge/following",
            manuallyApprovesFollowers = false
        )
        whenever(apUserService.getPersonByName(eq("hoge"))).doReturn(person)

        val objectMapper = ActivityPubConfig().objectMapper()

        mockMvc
            .get("/users/hoge")
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andExpect { content { this.json(objectMapper.writeValueAsString(person)) } }
    }

    @Test
    fun `userAP 存在しないユーザーにGETすると404が返ってくる`() = runTest {
        whenever(apUserService.getPersonByName(eq("fuga"))).doThrow(UserNotFoundException::class)

        mockMvc
            .get("/users/fuga")
            .asyncDispatch()
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `userAP POSTすると405が返ってくる`() {
        mockMvc
            .post("/users/hoge")
            .andExpect { status { isMethodNotAllowed() } }
    }
}
