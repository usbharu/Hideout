package dev.usbharu.hideout.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.net.URL

@Configuration
class SpringConfig {

    @Autowired
    lateinit var config: ApplicationConfig
}

@ConfigurationProperties("hideout")
data class ApplicationConfig(
    val url: URL
)
