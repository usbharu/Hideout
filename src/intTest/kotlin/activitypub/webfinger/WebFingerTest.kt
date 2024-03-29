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

package activitypub.webfinger

import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.application.external.Transaction
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import util.TestTransaction
import java.net.URL

@SpringBootTest(classes = [SpringApplication::class])
@AutoConfigureMockMvc
@Transactional
class WebFingerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @Sql("/sql/test-user.sql")

    fun `webfinger 存在するユーザーを取得`() {
        mockMvc
            .get("/.well-known/webfinger?resource=acct:test-user@example.com")
            .andExpect { status { isOk() } }
            .andExpect { header { string("Content-Type", "application/json") } }
            .andExpect {
                jsonPath("\$.subject") {
                    value("acct:test-user@example.com")
                }
            }
            .andExpect {
                jsonPath("\$.links[0].rel") {
                    value("self")
                }
            }
            .andExpect {
                jsonPath("\$.links[0].href") { value("https://example.com/users/test-user") }
            }
            .andExpect {
                jsonPath("\$.links[0].type") {
                    value("application/activity+json")
                }
            }
    }

    @Test
    fun `webfinger 存在しないユーザーに404`() {
        mockMvc
            .get("/.well-known/webfinger?resource=acct:invalid-user-notfound-afdjashfal@example.com")
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `webfinger 不正なリクエストは400`() {
        mockMvc
            .get("/.well-known/webfinger?res=acct:test")
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `webfinger acctのパースが出来なくても400`() {
        mockMvc
            .get("/.well-known/webfinger?resource=acct:@a@b@c@d")
            .andExpect { status { isBadRequest() } }
    }

    @TestConfiguration
    class Configuration {
        @Bean
        fun url(): URL {
            return URL("https://example.com")
        }

        @Bean
        fun testTransaction(): Transaction = TestTransaction
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
