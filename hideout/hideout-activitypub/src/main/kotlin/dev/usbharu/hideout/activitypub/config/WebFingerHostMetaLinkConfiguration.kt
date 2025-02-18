package dev.usbharu.hideout.activitypub.config

import dev.usbharu.hideout.activitypub.application.hostmeta.Link
import dev.usbharu.hideout.core.config.ApplicationConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebFingerHostMetaLinkConfiguration(private val applicationConfig: ApplicationConfig) {
    @Bean
    fun webFingerHostMetaLink(): Link {
        return Link(
            rel = "lrdd",
            type = "application/jrd+json",
            template = applicationConfig.url.resolve(".well-known/webfinger").toString() + "?resource={uri}"
        )
    }
}
