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

package dev.usbharu.hideout.application.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter
import java.net.URL

@Configuration
class SpringConfig {

    @Autowired
    lateinit var config: ApplicationConfig

    @Bean
    fun requestLoggingFilter(): CommonsRequestLoggingFilter {
        val loggingFilter = CommonsRequestLoggingFilter()
        loggingFilter.setIncludeHeaders(true)
        loggingFilter.setIncludeClientInfo(true)
        loggingFilter.setIncludeQueryString(true)
        loggingFilter.setIncludePayload(true)
        loggingFilter.setMaxPayloadLength(64000)
        return loggingFilter
    }
}

@ConfigurationProperties("hideout")
data class ApplicationConfig(
    val url: URL
)

@ConfigurationProperties("hideout.storage.s3")
@ConditionalOnProperty("hideout.storage.type", havingValue = "s3")
data class S3StorageConfig(
    val endpoint: String,
    val publicUrl: String,
    val bucket: String,
    val region: String,
    val accessKey: String,
    val secretKey: String
)

/**
 * メディアの保存にローカルファイルシステムを使用する際のコンフィグ
 *
 * @property path フォゾンする場所へのパス。 /から始めると絶対パスとなります。
 * @property publicUrl 公開用URL 省略可能 指定するとHideoutがファイルを配信しなくなります。
 */
@ConfigurationProperties("hideout.storage.local")
@ConditionalOnProperty("hideout.storage.type", havingValue = "local", matchIfMissing = true)
data class LocalStorageConfig(
    val path: String = "files",
    val publicUrl: String?
)

@ConfigurationProperties("hideout.character-limit")
data class CharacterLimit(
    val general: General = General(),
    val post: Post = Post(),
    val account: Account = Account(),
    val instance: Instance = Instance()
) {

    data class General(
        val url: Int = 1000,
        val domain: Int = 1000,
        val publicKey: Int = 10000,
        val privateKey: Int = 10000
    )

    data class Post(
        val text: Int = 3000,
        val overview: Int = 3000
    )

    data class Account(
        val id: Int = 300,
        val name: Int = 300,
        val description: Int = 10000
    )

    data class Instance(
        val name: Int = 600,
        val description: Int = 10000
    )
}
