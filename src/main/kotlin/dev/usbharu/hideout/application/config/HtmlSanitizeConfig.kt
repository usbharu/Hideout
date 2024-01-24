package dev.usbharu.hideout.application.config

import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HtmlSanitizeConfig {
    @Bean
    fun policy(): PolicyFactory {
        return HtmlPolicyBuilder()
            .allowElements("p")
            .allowElements("a")
            .allowElements("br")
            .allowAttributes("href").onElements("a")
            .allowUrlProtocols("http", "https")
            .allowElements({ _, _ -> return@allowElements "p" }, "h1", "h2", "h3", "h4", "h5", "h6")
            .toFactory()
    }
}
