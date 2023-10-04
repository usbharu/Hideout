package dev.usbharu.hideout.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.net.URL

@Configuration
class SpringConfig {

    @Autowired
    lateinit var config: ApplicationConfig

    @Autowired
    lateinit var storageConfig: StorageConfig
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
