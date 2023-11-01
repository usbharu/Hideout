package dev.usbharu.hideout.application.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter
import java.net.URL

@Configuration
class SpringConfig {

    @Autowired
    lateinit var config: ApplicationConfig

    @Autowired
    lateinit var storageConfig: StorageConfig

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

@ConfigurationProperties("hideout.storage")
data class StorageConfig(
    val useS3: Boolean,
    val endpoint: String,
    val publicUrl: String,
    val bucket: String,
    val region: String,
    val accessKey: String,
    val secretKey: String
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
