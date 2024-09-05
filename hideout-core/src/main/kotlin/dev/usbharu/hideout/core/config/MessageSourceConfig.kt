package dev.usbharu.hideout.core.config

import org.springframework.boot.autoconfigure.context.MessageSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.support.ReloadableResourceBundleMessageSource

@Configuration
@Profile("dev")
class MessageSourceConfig {
    @Bean
    fun messageSource(messageSourceProperties: MessageSourceProperties): MessageSource {
        val reloadableResourceBundleMessageSource = ReloadableResourceBundleMessageSource()
        reloadableResourceBundleMessageSource.setBasename("classpath:" + messageSourceProperties.basename)
        reloadableResourceBundleMessageSource.setCacheSeconds(0)
        return reloadableResourceBundleMessageSource
    }

    @Bean
    @Profile("dev")
    @ConfigurationProperties(prefix = "spring.messages")
    fun messageSourceProperties(): MessageSourceProperties {
        return MessageSourceProperties()
    }
}
