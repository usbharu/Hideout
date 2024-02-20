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

package dev.usbharu.hideout.activitypub.interfaces.api.inbox

import dev.usbharu.hideout.activitypub.domain.exception.JsonParseException
import dev.usbharu.hideout.activitypub.service.common.APService
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureHeaderChecker
import dev.usbharu.hideout.util.Base64Util
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.net.URI
import java.security.MessageDigest
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@ExtendWith(MockitoExtension::class)
class InboxControllerImplTest {

    private lateinit var mockMvc: MockMvc

    @Spy
    private val httpSignatureHeaderChecker =
        HttpSignatureHeaderChecker(ApplicationConfig(URI.create("https://example.com").toURL()))

    @Mock
    private lateinit var apService: APService

    @InjectMocks
    private lateinit var inboxController: InboxControllerImpl

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inboxController).build()
    }


    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)

    @Test
    fun `inbox 正常なPOSTリクエストをしたときAcceptが返ってくる`() = runTest {


        val json = """{"type":"Follow"}"""
        whenever(apService.parseActivity(eq(json))).doReturn(ActivityType.Follow)
        whenever(
            apService.processActivity(
                eq(json), eq(ActivityType.Follow), any(), any()

            )
        ).doReturn(Unit)

        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(json.toByteArray()))

        mockMvc.post("/inbox") {
            content = json
            contentType = MediaType.APPLICATION_JSON
            header("Signature", "a")
            header("Host", "example.com")
            header("Date", ZonedDateTime.now().format(dateTimeFormatter))
            header("Digest", digest)
        }.asyncDispatch().andExpect {
            status { isAccepted() }
        }

    }

    @Test
    fun `inbox parseActivityに失敗したときAcceptが返ってくる`() = runTest {
        val json = """{"type":"Hoge"}"""
        whenever(apService.parseActivity(eq(json))).doThrow(JsonParseException::class)
        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(json.toByteArray()))
        mockMvc.post("/inbox") {
            content = json
            contentType = MediaType.APPLICATION_JSON
            header("Signature", "a")
            header("Host", "example.com")
            header("Date", ZonedDateTime.now().format(dateTimeFormatter))
            header("Digest", digest)
        }.asyncDispatch().andExpect {
            status { isAccepted() }
        }

    }

    @Test
    fun `inbox processActivityに失敗したときAcceptが返ってくる`() = runTest {
        val json = """{"type":"Follow"}"""
        whenever(apService.parseActivity(eq(json))).doReturn(ActivityType.Follow)
        whenever(
            apService.processActivity(
                eq(json), eq(ActivityType.Follow), any(), any()
            )
        ).doThrow(FailedToGetResourcesException::class)
        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(json.toByteArray()))
        mockMvc.post("/inbox") {
            content = json
            contentType = MediaType.APPLICATION_JSON
            header("Signature", "a")
            header("Host", "example.com")
            header("Date", ZonedDateTime.now().format(dateTimeFormatter))
            header("Digest", digest)
        }.asyncDispatch().andExpect {
            status { isAccepted() }
        }

    }

    @Test
    fun `inbox GETリクエストには405を返す`() {
        mockMvc.get("/inbox").andExpect { status { isMethodNotAllowed() } }
    }

    @Test
    fun `user-inbox 正常なPOSTリクエストをしたときAcceptが返ってくる`() = runTest {


        val json = """{"type":"Follow"}"""
        whenever(apService.parseActivity(eq(json))).doReturn(ActivityType.Follow)
        whenever(apService.processActivity(eq(json), eq(ActivityType.Follow), any(), any())).doReturn(
            Unit
        )
        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(json.toByteArray()))
        mockMvc.post("/users/hoge/inbox") {
            content = json
            contentType = MediaType.APPLICATION_JSON
            header("Signature", "a")
            header("Host", "example.com")
            header("Date", ZonedDateTime.now().format(dateTimeFormatter))
            header("Digest", digest)
        }.asyncDispatch().andExpect {
            status { isAccepted() }
        }

    }

    @Test
    fun `user-inbox parseActivityに失敗したときAcceptが返ってくる`() = runTest {
        val json = """{"type":"Hoge"}"""
        whenever(apService.parseActivity(eq(json))).doThrow(JsonParseException::class)
        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(json.toByteArray()))
        mockMvc.post("/users/hoge/inbox") {
            content = json
            contentType = MediaType.APPLICATION_JSON
            header("Signature", "a")
            header("Host", "example.com")
            header("Date", ZonedDateTime.now().format(dateTimeFormatter))
            header("Digest", digest)
        }.asyncDispatch().andExpect {
            status { isAccepted() }
        }

    }

    @Test
    fun `user-inbox processActivityに失敗したときAcceptが返ってくる`() = runTest {
        val json = """{"type":"Follow"}"""
        whenever(apService.parseActivity(eq(json))).doReturn(ActivityType.Follow)
        whenever(
            apService.processActivity(
                eq(json), eq(ActivityType.Follow), any(), any()
            )
        ).doThrow(FailedToGetResourcesException::class)
        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(json.toByteArray()))
        mockMvc.post("/users/hoge/inbox") {
            content = json
            contentType = MediaType.APPLICATION_JSON
            header("Signature", "a")
            header("Host", "example.com")
            header("Date", ZonedDateTime.now().format(dateTimeFormatter))
            header("Digest", digest)
        }.asyncDispatch().andExpect {
            status { isAccepted() }
        }

    }

    @Test
    fun `user-inbox GETリクエストには405を返す`() {
        mockMvc.get("/users/hoge/inbox").andExpect { status { isMethodNotAllowed() } }
    }

    @Test
    fun `inbox Dateヘッダーが無いと400`() {
        val json = """{"type":"Follow"}"""
        mockMvc
            .post("/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
            }
            .asyncDispatch()
            .andExpect {
                status {
                    isBadRequest()
                }
            }
    }

    @Test
    fun `user-inbox Dateヘッダーが無いと400`() {
        val json = """{"type":"Follow"}"""
        mockMvc
            .post("/users/hoge/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
            }
            .asyncDispatch()
            .andExpect {
                status {
                    isBadRequest()
                }
            }
    }

    @Test
    fun `inbox Dateヘッダーが未来だと401`() {
        val json = """{"type":"Follow"}"""
        mockMvc
            .post("/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Date", ZonedDateTime.now().plusDays(1).format(dateTimeFormatter))
            }
            .asyncDispatch()
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `user-inbox Dateヘッダーが未来だと401`() {
        val json = """{"type":"Follow"}"""
        mockMvc
            .post("/users/hoge/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Date", ZonedDateTime.now().plusDays(1).format(dateTimeFormatter))
            }
            .asyncDispatch()
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `inbox Dateヘッダーが過去過ぎると401`() {
        val json = """{"type":"Follow"}"""
        mockMvc
            .post("/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Date", ZonedDateTime.now().minusDays(1).format(dateTimeFormatter))
            }
            .asyncDispatch()
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `user-inbox Dateヘッダーが過去過ぎると401`() {
        val json = """{"type":"Follow"}"""
        mockMvc
            .post("/users/hoge/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Date", ZonedDateTime.now().minusDays(1).format(dateTimeFormatter))
            }
            .asyncDispatch()
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `inbox Hostヘッダーが無いと400`() {
        val json = """{"type":"Follow"}"""
        mockMvc
            .post("/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Date", ZonedDateTime.now().format(dateTimeFormatter))
            }
            .asyncDispatch()
            .andExpect {
                status {
                    isBadRequest()
                }
            }
    }

    @Test
    fun `user-inbox Hostヘッダーが無いと400`() {
        val json = """{"type":"Follow"}"""
        mockMvc
            .post("/users/hoge/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Date", ZonedDateTime.now().format(dateTimeFormatter))
            }
            .asyncDispatch()
            .andExpect {
                status {
                    isBadRequest()
                }
            }
    }

    @Test
    fun `inbox Hostヘッダーが間違ってると401`() {
        val json = """{"type":"Follow"}"""
        mockMvc
            .post("/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Date", ZonedDateTime.now().format(dateTimeFormatter))
                header("Host", "example.jp")
            }
            .asyncDispatch()
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `user-inbox Hostヘッダーが間違ってると401`() {
        val json = """{"type":"Follow"}"""
        mockMvc
            .post("/users/hoge/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Date", ZonedDateTime.now().format(dateTimeFormatter))
                header("Host", "example.jp")
            }
            .asyncDispatch()
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `inbox Digestヘッダーがないと400`() = runTest {


        val json = """{"type":"Follow"}"""

        mockMvc
            .post("/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Signature", "")
                header("Host", "example.com")
                header("Date", ZonedDateTime.now().format(dateTimeFormatter))
            }
            .asyncDispatch()
            .andExpect {
                status { isBadRequest() }
            }

    }

    @Test
    fun `inbox Digestヘッダーが間違ってると401`() = runTest {
        val json = """{"type":"Follow"}"""
        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(("$json aaaaaaaa").toByteArray()))

        mockMvc
            .post("/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Signature", "")
                header("Host", "example.com")
                header("Date", ZonedDateTime.now().format(dateTimeFormatter))
                header("Digest", digest)
            }
            .asyncDispatch()
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `user-inbox Digestヘッダーがないと400`() = runTest {


        val json = """{"type":"Follow"}"""

        mockMvc
            .post("/users/hoge/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Signature", "")
                header("Host", "example.com")
                header("Date", ZonedDateTime.now().format(dateTimeFormatter))
            }
            .asyncDispatch()
            .andExpect {
                status { isBadRequest() }
            }

    }

    @Test
    fun `user-inbox Digestヘッダーが間違ってると401`() = runTest {
        val json = """{"type":"Follow"}"""
        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(("$json aaaaaaaa").toByteArray()))

        mockMvc
            .post("/users/hoge/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Signature", "")
                header("Host", "example.com")
                header("Date", ZonedDateTime.now().format(dateTimeFormatter))
                header("Digest", digest)
            }
            .asyncDispatch()
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `inbox Signatureヘッダーがないと401`() = runTest {


        val json = """{"type":"Follow"}"""
        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(json.toByteArray()))

        mockMvc
            .post("/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Host", "example.com")
                header("Date", ZonedDateTime.now().format(dateTimeFormatter))
                header("Digest", digest)
            }
            .asyncDispatch()
            .andExpect {
                status { isUnauthorized() }
            }

    }

    @Test
    fun `inbox Signatureヘッダーが空だと401`() = runTest {
        val json = """{"type":"Follow"}"""
        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(json.toByteArray()))

        mockMvc
            .post("/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Signature", "")
                header("Host", "example.com")
                header("Date", ZonedDateTime.now().format(dateTimeFormatter))
                header("Digest", digest)
            }
            .asyncDispatch()
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `user-inbox Digestヘッダーがないと401`() = runTest {

        val json = """{"type":"Follow"}"""
        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(json.toByteArray()))
        mockMvc
            .post("/users/hoge/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Host", "example.com")
                header("Date", ZonedDateTime.now().format(dateTimeFormatter))
                header("Digest", digest)
            }
            .asyncDispatch()
            .andExpect {
                status { isUnauthorized() }
            }

    }

    @Test
    fun `user-inbox Digestヘッダーが空だと401`() = runTest {
        val json = """{"type":"Follow"}"""
        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(json.toByteArray()))

        mockMvc
            .post("/users/hoge/inbox") {
                content = json
                contentType = MediaType.APPLICATION_JSON
                header("Signature", "")
                header("Host", "example.com")
                header("Date", ZonedDateTime.now().format(dateTimeFormatter))
                header("Digest", digest)
            }
            .asyncDispatch()
            .andExpect {
                status { isUnauthorized() }
            }
    }
}