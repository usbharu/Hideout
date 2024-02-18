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

package federation

import AssertionUtil
import KarateUtil
import com.intuit.karate.core.MockServer
import com.intuit.karate.junit5.Karate
import dev.usbharu.hideout.SpringApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.transaction.annotation.Transactional
import java.net.MalformedURLException
import java.net.URL

@SpringBootTest(
    classes = [SpringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Transactional
class FollowAcceptTest {
    @LocalServerPort
    private var port = ""

    @Karate.Test
    @TestFactory
    @Disabled
    fun `FollowAcceptTest`(): Karate {
        return KarateUtil.e2eTest(
            "FollowAcceptTest", "Follow Accept Test",
            mapOf("karate.port" to port),
            javaClass
        )
    }

    companion object {
        lateinit var server: MockServer

        lateinit var _remotePort: String

        @JvmStatic
        fun assertUserExist(username: String, domain: String) = runBlocking {
            val s = try {
                val url = URL(domain)
                url.host + ":" + url.port.toString().takeIf { it != "-1" }.orEmpty()
            } catch (e: MalformedURLException) {
                domain
            }

            var check = false

            repeat(10) {
                delay(1000)
                check = AssertionUtil.assertUserExist(username, s) or check
                if (check) {
                    return@repeat
                }
            }

            Assertions.assertTrue(check, "User @$username@$s not exist.")
        }

        @JvmStatic
        fun getRemotePort(): String = _remotePort

        @BeforeAll
        @JvmStatic
        fun beforeAll(@Autowired flyway: Flyway) {
            server = MockServer.feature("classpath:federation/FollowAcceptMockServer.feature").http(0).build()
            _remotePort = server.port.toString()

            flyway.clean()
            flyway.migrate()
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            server.stop()
        }
    }
}
