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

package mastodon.notifications

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.SpringApplication
import dev.usbharu.hideout.domain.mastodon.model.generated.Notification
import dev.usbharu.testmvc.Configuration
import dev.usbharu.testmvc.TestDefinition
import dev.usbharu.testmvc.TestMvc
import dev.usbharu.testmvc.path.Path
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(classes = [SpringApplication::class], properties = ["hideout.use-mongodb=false"])
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/test-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql("/sql/test-user2.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql("/sql/notification/test-notifications.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql("/sql/notification/test-mastodon_notifications.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class NotificationTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    private lateinit var testMvc: TestMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
        testMvc = TestMvc(mockMvc, Configuration(jacksonObjectMapper()))
    }

    @TestFactory
    fun `apiV1NotificationsIdGet 取得できる`() = testMvc.request(
        TestDefinition(
            Path("api", "v1", "notifications", arrayOf("1", "2")),
            HttpMethod.GET,
            HttpStatus.OK,
            {
                with(jwt().jwt { it.claim("uid", "1") }.authorities(SimpleGrantedAuthority("SCOPE_read")))
            },
            Notification::class
        )
    )

    companion object {
        @JvmStatic
        @AfterAll
        fun dropDatabase(@Autowired flyway: Flyway) {
            flyway.clean()
            flyway.migrate()
        }
    }
}