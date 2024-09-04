package dev.usbharu.hideout.core.config

import dev.usbharu.hideout.core.infrastructure.springframework.SPAInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(private val spaInterceptor: SPAInterceptor) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(spaInterceptor)
    }
}