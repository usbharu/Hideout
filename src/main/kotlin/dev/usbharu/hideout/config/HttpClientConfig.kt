package dev.usbharu.hideout.config

import dev.usbharu.hideout.plugins.KtorKeyMap
import dev.usbharu.hideout.plugins.httpSignaturePlugin
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.logging.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tech.barbero.http.message.signing.KeyMap

@Configuration
class HttpClientConfig {
    @Bean
    fun httpClient(keyMap: KeyMap): HttpClient = HttpClient(CIO).config {
        install(httpSignaturePlugin) {
            this.keyMap = keyMap
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(HttpCache) {
        }
        expectSuccess = true
    }

    @Bean
    fun keyMap(
        userQueryService: UserQueryService,
        transaction: Transaction,
        applicationConfig: ApplicationConfig
    ): KtorKeyMap {
        return KtorKeyMap(
            userQueryService,
            transaction,
            applicationConfig
        )
    }
}
