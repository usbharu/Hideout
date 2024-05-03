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

package oauth2

import KarateUtil
import com.intuit.karate.junit5.Karate
import dev.usbharu.hideout.SpringApplication
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql

@SpringBootTest(
    classes = [SpringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@Sql("/oauth2/user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class OAuth2LoginTest {

    @LocalServerPort
    private var port = ""

    @Karate.Test
    @TestFactory
    fun `スコープwrite readを持ったトークンの作成`(): Karate {
        return KarateUtil.springBootKarateTest(
            "Oauth2LoginTest",
            "スコープwrite readを持ったトークンの作成",
            javaClass,
            port
        )
    }

    @Karate.Test
    @TestFactory
    fun `スコープread_statuses write_statusesを持ったトークンの作成`(): Karate {
        return KarateUtil.springBootKarateTest(
            "Oauth2LoginTest",
            "スコープread:statuses write:statusesを持ったトークンの作成",
            javaClass,
            port
        )
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
