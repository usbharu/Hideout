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
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isNotImplemented() } }
    }

    @Test
    fun `user-outbox GETに501を返す`() {
        mockMvc
            .get("/users/hoge/outbox")
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isNotImplemented() } }
    }

    @Test
    fun `outbox POSTに501を返す`() {
        mockMvc
            .post("/outbox")
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isNotImplemented() } }
    }

    @Test
    fun `user-outbox POSTに501を返す`() {
        mockMvc
            .post("/users/hoge/outbox")
            .asyncDispatch()
            .andDo { print() }
            .andExpect { status { isNotImplemented() } }
    }
}
