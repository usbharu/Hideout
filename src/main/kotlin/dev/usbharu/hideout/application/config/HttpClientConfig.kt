package dev.usbharu.hideout.application.config

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.logging.*
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpClientConfig {
    @Bean
    fun httpClient(buildProperties: BuildProperties, applicationConfig: ApplicationConfig): HttpClient =
        HttpClient(CIO).config {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        install(HttpCache) {
        }
        expectSuccess = true
            install(UserAgent) {
                agent = "Hideout/${buildProperties.version} (${applicationConfig.url})"
            }
    }
}
